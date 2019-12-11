package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.ModConstants;
import com.mercuriusxeno.mercurialtools.util.AlignedField;
import com.mercuriusxeno.mercurialtools.util.LastPosition;
import com.mercuriusxeno.mercurialtools.util.ProjectedField;
import com.mercuriusxeno.mercurialtools.util.enums.AlignmentBias;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EnderKeystoneTile extends TileEntity implements ITickableTileEntity {
    public EnderKeystoneTile() {
        super(ModBlocks.ENDER_KEYSTONE_TILE);
    }

    private static final ProjectedField WARPFIELD_AREA =
            new ProjectedField(AlignmentBias.CENTER, AlignmentBias.CENTER, AlignmentBias.NEGATIVE,
                    ModConstants.ENDER_KEYSTONE_OFFSET_HEIGHT, ModConstants.ENDER_KEYSTONE_FIELD_HEIGHT,
                    ModConstants.ENDER_KEYSTONE_OFFSET_WIDTH, ModConstants.ENDER_KEYSTONE_FIELD_WIDTH,
                    ModConstants.ENDER_KEYSTONE_OFFSET_DEPTH, ModConstants.ENDER_KEYSTONE_FIELD_DEPTH);


    private AlignedField warpField = null;
    private AlignedField getWarpField() {
        if (warpField == null) {
            // offset the projected field forward (by facing) one block so that it aligns with the face and not the back.
            warpField = WARPFIELD_AREA
                    .getProjectedField(
                            this.pos.offset(this.getBlockState().get(EnderKeystone.FACING)),
                            this.getBlockState().get(EnderKeystone.FACING),
                            this.getBlockState().get(EnderKeystone.FACING) == Direction.UP || this.getBlockState().get(EnderKeystone.FACING) == Direction.DOWN ?
                                    Direction.NORTH : Direction.UP
                    );
        }
        return warpField;
    }

    private AxisAlignedBB warpBox = null;
    private AxisAlignedBB getWarpBox() {
        if (warpBox == null) {
            // Direction direction = this.getBlockState().get(EnderKeystone.FACING);
            warpBox = getWarpField().getBoundingBox().expand(1, 1, 1);
        }
        return warpBox;
    }

    private Vec3d getEntityPosition(Entity e) {
        return new Vec3d(e.posX, e.posY, e.posZ);
    }

    private boolean isStalePosition(LastPosition lastPosition) {
        if (this.world == null) {
            return true;
        }
        return this.world.getGameTime() - 1 > lastPosition.tickFor;
    }

    private LastPosition getTrackedPosition(int entityId) {
        if (this.world == null) {
            return null;
        }
        for(LastPosition trackedPosition : trackedPositions) {
            if (trackedPosition.entityId == entityId && this.world.getGameTime() - 1 == trackedPosition.tickFor) {
                return trackedPosition;
            }
        }
        return null;
    }

    private void purgeStalePositions() {
        ArrayList<LastPosition> stalePositions = new ArrayList<>();
        for(LastPosition trackedPosition : trackedPositions) {
            if (isStalePosition(trackedPosition)) {
                stalePositions.add(trackedPosition);
            }
        }

        for(LastPosition stalePosition : stalePositions) {
            trackedPositions.removeIf(t -> t.entityId == stalePosition.entityId && t.lastPosition.equals(stalePosition.lastPosition));
        }
    }

    private ArrayList<LastPosition> trackedPositions = new ArrayList<>();
    @Override
    public void tick() {
        if (this.world == null || this.world.isRemote()) {
            return;
        }

        List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, getWarpBox());
        for(Entity e : entities) {
            // the game doesn't track entity movement very well. here, we do it for them
            LastPosition trackedPosition = getTrackedPosition(e.getEntityId());
            if (trackedPosition != null) {
                // do movement instead
                Vec3d movementVector = trackedPosition.getMovementVector(getEntityPosition(e), getWarpFactor());
                // that's not how we use these to go downward.
                if (movementVector.z < 0.0D) {
                    continue;
                }
                // hey, this thing ain't movin.
                if (movementVector.x == 0.0D && movementVector.y == 0.0D && movementVector.z == 0.0D) {
                    // if we're sneaking, that's how we go down
                    if (e.isSneaking()) {
                        movementVector = new Vec3d(0.0D, -getWarpFactor(), 0.0D);
                    } else {
                        // if not, we just don't do anything
                        continue;
                    }
                }
                // now we can test if we're going laterally
                if (Math.abs(movementVector.x) > 0.0D || Math.abs(movementVector.z) > 0.0D) {
                    // if we're not sprinting, sucks to be you, you're not going fast.
                    if (!e.isSprinting()) {
                        continue;
                    }
                }
                Vec3d newPositionVec = movementVector.add(e.getPositionVec());
                Vec3d newPosition = new Vec3d(newPositionVec.x, newPositionVec.y, newPositionVec.z);
                if (canTeleport(e, newPosition)) {
                    e.teleportKeepLoaded(newPosition.x, newPosition.y, newPosition.z);
                    if (e instanceof PlayerEntity && Math.abs(movementVector.y) > 0.0D) {
                        // if we got mad ups, give us a hand with slow falling.
                        ((PlayerEntity) e).addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, getSlowFallAssistance(), 2));
                    }
                }
            } else {
                trackedPositions.add(new LastPosition(getEntityPosition(e), e.getEntityId(), this.world.getGameTime()));
            }
        }

        purgeStalePositions();
    }

    private int getSlowFallAssistance() {
        return MathHelper.floor(getWarpFactor() * 4);
    }

    private boolean canTeleport(Entity e, Vec3d newPosition) {
        if (this.world == null) {
            return false;
        }
        int blockHeight = (int)Math.ceil(e.getHeight());
        int blockWidth = (int)Math.ceil(e.getWidth());
        BlockPos blockPos = new BlockPos(newPosition.x, newPosition.y, newPosition.z);
        BlockPos endPos = blockPos.offset(Direction.SOUTH, blockWidth).offset(Direction.EAST, blockWidth).offset(Direction.UP, blockHeight);
        Iterator<BlockPos> blocksToCheck = BlockPos.getAllInBox(blockPos, endPos).iterator();
        while(blocksToCheck.hasNext()) {
            BlockPos blockToCheck = blocksToCheck.next();
            if (!this.world.getBlockState(blockToCheck).allowsMovement(this.world, blockToCheck, PathType.AIR)) {
                return false;
            }
        }
        return true;
    }

    private int dampening = -1;
    private int getDampening() {
        if (this.world == null) {
            return 0;
        }
        if (dampening == -1) {
            Block blockFound = this.world.getBlockState(this.getPos()).getBlock();
            if (!(blockFound instanceof EnderKeystone)) {
                return 0;
            }

            dampening = MathHelper.ceil(Math.pow(2, ((EnderKeystone)blockFound).getDampening()));
        }
        return dampening;
    }

    public double getWarpFactor() {
        if (this.world == null) {
            return 0.0D;
        }
        return ModConstants.ENDER_KEYSTONE_WARP_FACTOR / getDampening();
    }
}
