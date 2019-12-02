package com.mercuriusxeno.mercurialtools.tileentity;

import com.mercuriusxeno.mercurialtools.block.EnderVacuum;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

public class EnderVacuumTileEntity extends LockableLootTileEntity implements ISidedInventory, ITickableTileEntity {
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openCount;
    private AnimationStatus animationStatus = AnimationStatus.CLOSED;
    private float progress;
    private float progressOld;

    public EnderVacuumTileEntity() {
        super(TileEntityType.SHULKER_BOX);
    }

    public void tick() {
        this.updateAnimation();
        if (this.animationStatus == AnimationStatus.OPENING || this.animationStatus == AnimationStatus.CLOSING) {
            this.moveCollidedEntities();
        }
    }

    protected void updateAnimation() {
        this.progressOld = this.progress;
        switch(this.animationStatus) {
            case CLOSED:
                this.progress = 0.0F;
                break;
            case OPENING:
                this.progress += 0.1F;
                if (this.progress >= 1.0F) {
                    this.moveCollidedEntities();
                    this.animationStatus = AnimationStatus.OPENED;
                    this.progress = 1.0F;
                    this.updateNeighborStates();
                }
                break;
            case CLOSING:
                this.progress -= 0.1F;
                if (this.progress <= 0.0F) {
                    this.animationStatus = AnimationStatus.CLOSED;
                    this.progress = 0.0F;
                    this.updateNeighborStates();
                }
                break;
            case OPENED:
                this.progress = 1.0F;
        }

    }

    public AnimationStatus getAnimationStatus() {
        return this.animationStatus;
    }

    public AxisAlignedBB getBoundingBox(BlockState blockState) {
        return this.getBoundingBox(blockState.get(EnderVacuum.FACING));
    }

    public AxisAlignedBB getBoundingBox(Direction p_190587_1_) {
        float f = this.getProgress(1.0F);
        return VoxelShapes.fullCube().getBoundingBox().expand((double)(0.5F * f * (float)p_190587_1_.getXOffset()), (double)(0.5F * f * (float)p_190587_1_.getYOffset()), (double)(0.5F * f * (float)p_190587_1_.getZOffset()));
    }

    private AxisAlignedBB getTopBoundingBox(Direction p_190588_1_) {
        Direction direction = p_190588_1_.getOpposite();
        return this.getBoundingBox(p_190588_1_).contract((double)direction.getXOffset(), (double)direction.getYOffset(), (double)direction.getZOffset());
    }

    private void moveCollidedEntities() {
        BlockState blockstate = this.world.getBlockState(this.getPos());
        if (blockstate.getBlock() instanceof EnderVacuum) {
            Direction direction = blockstate.get(EnderVacuum.FACING);
            AxisAlignedBB axisalignedbb = this.getTopBoundingBox(direction).offset(this.pos);
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity((Entity)null, axisalignedbb);
            if (!list.isEmpty()) {
                for(int i = 0; i < list.size(); ++i) {
                    Entity entity = list.get(i);
                    if (entity.getPushReaction() != PushReaction.IGNORE) {
                        double d0 = 0.0D;
                        double d1 = 0.0D;
                        double d2 = 0.0D;
                        AxisAlignedBB axisalignedbb1 = entity.getBoundingBox();
                        switch(direction.getAxis()) {
                            case X:
                                if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                                    d0 = axisalignedbb.maxX - axisalignedbb1.minX;
                                } else {
                                    d0 = axisalignedbb1.maxX - axisalignedbb.minX;
                                }

                                d0 = d0 + 0.01D;
                                break;
                            case Y:
                                if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                                    d1 = axisalignedbb.maxY - axisalignedbb1.minY;
                                } else {
                                    d1 = axisalignedbb1.maxY - axisalignedbb.minY;
                                }

                                d1 = d1 + 0.01D;
                                break;
                            case Z:
                                if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                                    d2 = axisalignedbb.maxZ - axisalignedbb1.minZ;
                                } else {
                                    d2 = axisalignedbb1.maxZ - axisalignedbb.minZ;
                                }

                                d2 = d2 + 0.01D;
                        }

                        entity.move(MoverType.SHULKER_BOX, new Vec3d(d0 * (double)direction.getXOffset(), d1 * (double)direction.getYOffset(), d2 * (double)direction.getZOffset()));
                    }
                }

            }
        }
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory() {
        return this.items.size();
    }

    /**
     * This must return true serverside before it is called clientside.
     */
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.openCount = type;
            if (type == 0) {
                this.animationStatus = AnimationStatus.CLOSING;
                this.updateNeighborStates();
            }

            if (type == 1) {
                this.animationStatus = AnimationStatus.OPENING;
                this.updateNeighborStates();
            }

            return true;
        } else {
            return super.receiveClientEvent(id, type);
        }
    }

    private void updateNeighborStates() {
        this.getBlockState().updateNeighbors(this.getWorld(), this.getPos(), 3);
    }

    public void openInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }

            ++this.openCount;
            this.world.addBlockEvent(this.pos, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount == 1) {
                this.world.playSound(null, this.pos, SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
            }
        }
    }

    public void closeInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.openCount;
            this.world.addBlockEvent(this.pos, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount <= 0) {
                this.world.playSound(null, this.pos, SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
            }
        }

    }

    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.enderVacuum");
    }

    public void read(CompoundNBT compound) {
        super.read(compound);
        this.loadFromNbt(compound);
    }

    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        return this.saveToNbt(compound);
    }

    public void loadFromNbt(CompoundNBT compound) {
        this.items = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(compound) && compound.contains("Items", 9)) {
            ItemStackHelper.loadAllItems(compound, this.items);
        }
    }

    public CompoundNBT saveToNbt(CompoundNBT compound) {
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.items, false);
        }

        return compound;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.items = itemsIn;
    }

    public boolean isEmpty() {
        for(ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    /**
     * Returns true if automation can insert the given item in the given slot from the given side.
     */
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return !(Block.getBlockFromItem(itemStackIn.getItem()) instanceof EnderVacuum);
    }

    /**
     * Returns true if automation can extract the given item in the given slot from the given side.
     */
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public float getProgress(float lerpProgress) {
        return MathHelper.lerp(lerpProgress, this.progressOld, this.progress);
    }

    protected Container createMenu(int id, PlayerInventory player) {
        return new ShulkerBoxContainer(id, player, this);
    }

    @Override
    protected IItemHandler createUnSidedHandler() {
        return new SidedInvWrapper(this, Direction.UP);
    }

    public enum AnimationStatus {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING;
    }
}
