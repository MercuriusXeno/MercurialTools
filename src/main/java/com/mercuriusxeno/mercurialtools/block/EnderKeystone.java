package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.Constants;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.AlignedField;
import com.mercuriusxeno.mercurialtools.util.ProjectedField;
import com.mercuriusxeno.mercurialtools.util.enums.AlignmentBias;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class EnderKeystone extends DirectionalBlock {
    public EnderKeystone() {
        super(Block.Properties
                .create(Material.ROCK)
                .sound(SoundType.STONE)
                .hardnessAndResistance(3.0f)
                .lightValue(0));
        setRegistryName(Names.ENDER_KEYSTONE);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH));
    }
    public static final ProjectedField WARPFIELD =
            new ProjectedField(AlignmentBias.CENTER, AlignmentBias.CENTER, AlignmentBias.NEGATIVE,
                    Constants.ENDER_KEYSTONE_OFFSET_HEIGHT, Constants.ENDER_KEYSTONE_FIELD_HEIGHT,
                    Constants.ENDER_KEYSTONE_OFFSET_WIDTH, Constants.ENDER_KEYSTONE_FIELD_WIDTH,
                    Constants.ENDER_KEYSTONE_OFFSET_DEPTH, Constants.ENDER_KEYSTONE_FIELD_DEPTH);

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * fine.
     */
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (worldIn == null || worldIn.isRemote) {
            return;
        }

        // get the block facing, as it determines the projected "death field"
        Direction faceDirection = state.get(FACING);

        // TODO - make this work
        AlignedField warpField = WARPFIELD.getProjectedField(pos, faceDirection, Direction.UP);

        AxisAlignedBB boundingBox = warpField.getBoundingBox();

        // TODO - make this use a whitelist
        // check if there are any living entities that aren't players in the area.
        // make sure those entities are whitelisted for murder via interloper.

        List<Entity> entities = worldIn.getEntitiesWithinAABB(Entity.class, boundingBox);
        for(Entity e : entities) {
            // TODO - move the entities to the next keystone in the adjacent chunk if one exists
            // TODO - base it on the movement velocity of the entity
        }

        // schedule the next tick
        worldIn.getPendingBlockTicks().scheduleTick(pos, this, Constants.ENDER_KEYSTONE_UPDATE_COOLDOWN);
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        // the keystone's placement "front" is toward the player instead of away.
        Direction facing = context.getNearestLookingDirection().getOpposite();
        return this.getDefaultState().with(FACING, facing);
    }
}
