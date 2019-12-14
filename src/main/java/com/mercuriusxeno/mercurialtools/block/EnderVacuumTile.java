package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.ModConstants;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class EnderVacuumTile extends LockableLootTileEntity implements ISidedInventory, ITickableTileEntity {
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private ArrayList<Integer> trackedItemIds = new ArrayList<>();
    private int openCount;
    private AnimationStatus animationStatus = AnimationStatus.CLOSED;
    private float progress;
    private float progressOld;
    private boolean wasPullingItem = false;

    public EnderVacuumTile() {
        super(ModBlocks.ENDER_VACUUM_TILE);
    }

    public void tick() {
        this.updateAnimation();
        if (this.animationStatus == AnimationStatus.OPENING || this.animationStatus == AnimationStatus.CLOSING) {
            this.moveCollidedEntities();
        }

        if (this.getWorld().isRemote()) {
            return;
        }

        // do suction if an item is nearby, this is the whole point of a Vacuum chest, isn't it?
        if (isPullingItem()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }
            if (this.openCount == 0) {
                ++this.openCount;
            }
            this.wasPullingItem = true;
            this.world.addBlockEvent(this.pos, this.getBlockState().getBlock(), 1, this.openCount);
        } else {
            if (this.wasPullingItem) {
                if (this.openCount > 0) {
                    --this.openCount;
                }
                if (this.openCount < 0) {
                    this.openCount = 0;
                }
                this.wasPullingItem = false;
                this.world.addBlockEvent(this.pos, this.getBlockState().getBlock(), 1, this.openCount);
            }
        }
    }

    private double getDistanceToEntity(Entity e) {
        return Math.sqrt(e.getDistanceSq(this.getVacuumDestination()));
    }

    private boolean isPullingItem() {
        if (this.getWorld() == null) {
            return false;
        }
        AxisAlignedBB tileBox = this.getBoundingBox(this.getBlockState()).offset(getVacuumDestination());
        // "configurable" block radius determines how far to grab entity items from
        List<ItemEntity> items = this.getWorld().getEntitiesWithinAABB(ItemEntity.class, tileBox.grow(ModConstants.ENDER_VACUUM_RANGE), e -> getDistanceToEntity(e) <= ModConstants.ENDER_VACUUM_RANGE);

        // track items first
        for(ItemEntity item : items) {
            if (!trackedItemIds.contains(item.getEntityId())) {
                trackedItemIds.add(item.getEntityId());
            }
        }

        // make sure we're looking at the right items
        List<ItemEntity> trackedItemEntities = new ArrayList<>();
        List<Integer> stopTrackingTheseIds = new ArrayList<>();
        for (Integer trackedItemId : trackedItemIds) {
            Entity e = this.getWorld().getEntityByID(trackedItemId);
            if (!(e instanceof ItemEntity)) {
                stopTrackingTheseIds.add(trackedItemId);
                continue;
            }
            trackedItemEntities.add((ItemEntity) e);
        }

        for(int i = 0 ; i < stopTrackingTheseIds.size(); i++) {
            int finalI = i;
            trackedItemIds.removeIf(t -> t.equals(stopTrackingTheseIds.get(finalI)));
        }

        boolean isGrabbingItem = false;
        for (ItemEntity item : trackedItemEntities) {
            // no clip here weirdly didn't seem to work. trying with null bounding box instead...
            if (getVacuumDestination().distanceTo(getItemPosition(item)) > ModConstants.UNIT_CUBE_CORNER_DISTANCE_COEFFICIENT) {
                item.setBoundingBox(ModConstants.EMPTY_BOUNDING_BOX.offset(item.getPositionVec()));
                item.setNoGravity(true);
                isGrabbingItem = true;
                Vec3d vacuumVector = getVacuumVector(item).normalize().scale(0.5D); // slow it down a little
                item.setVelocity(vacuumVector.x, vacuumVector.y, vacuumVector.z);
            } else {
                if (captureItem(this, item)) {
                    // we don't do anything for now
                }
                // bounding box is set organically by Entity.setPosition - I think just stopping the bounding box override puts the bounding box back to defaults.
                item.setNoGravity(false);
            }
        }
        return isGrabbingItem;
    }

    public static boolean captureItem(IInventory inventory, ItemEntity itemEntity) {
        boolean flag = false;
        ItemStack itemstack = itemEntity.getItem().copy();
        ItemStack itemstack1 = putStackInInventoryAllSlots(null, inventory, itemstack, null);
        if (itemstack1.isEmpty()) {
            flag = true;
            itemEntity.remove();
        } else {
            itemEntity.setItem(itemstack1);
        }

        return flag;
    }

    /**
     * Attempts to place the passed stack in the inventory, using as many slots as required. Returns leftover items
     */
    public static ItemStack putStackInInventoryAllSlots(@Nullable IInventory source, IInventory destination, ItemStack stack, @Nullable Direction direction) {
        if (destination instanceof ISidedInventory && direction != null) {
            ISidedInventory isidedinventory = (ISidedInventory)destination;
            int[] aint = isidedinventory.getSlotsForFace(direction);

            for(int k = 0; k < aint.length && !stack.isEmpty(); ++k) {
                stack = insertStack(source, destination, stack, aint[k], direction);
            }
        } else {
            int i = destination.getSizeInventory();

            for(int j = 0; j < i && !stack.isEmpty(); ++j) {
                stack = insertStack(source, destination, stack, j, direction);
            }
        }

        return stack;
    }

    private static boolean canInsertItemInSlot(IInventory inventoryIn, ItemStack stack, int index, @Nullable Direction side) {
        if (!inventoryIn.isItemValidForSlot(index, stack)) {
            return false;
        } else {
            return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canInsertItem(index, stack, side);
        }
    }

    private static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) {
            return false;
        } else if (stack1.getDamage() != stack2.getDamage()) {
            return false;
        } else if (stack1.getCount() > stack1.getMaxStackSize()) {
            return false;
        } else {
            return ItemStack.areItemStackTagsEqual(stack1, stack2);
        }
    }

    /**
     * Insert the specified stack to the specified inventory and return any leftover items
     */
    private static ItemStack insertStack(@Nullable IInventory source, IInventory destination, ItemStack stack, int index, @Nullable Direction direction) {
        ItemStack itemstack = destination.getStackInSlot(index);
        if (canInsertItemInSlot(destination, stack, index, direction)) {
            if (itemstack.isEmpty()) {
                destination.setInventorySlotContents(index, stack);
                stack = ItemStack.EMPTY;
            } else if (canCombine(itemstack, stack)) {
                int i = stack.getMaxStackSize() - itemstack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.shrink(j);
                itemstack.grow(j);
            }
        }

        return stack;
    }

    private Vec3d getVacuumDestination() {
        return new Vec3d (this.getPos().getX() + 0.5D, this.getPos().getY() + 0.5D, this.getPos().getZ() + 0.5D);
    }
    
    private Vec3d getVacuumVector(ItemEntity itemEntity) {
        return getVacuumDestination().subtract(getItemPosition(itemEntity));
    }

    private Vec3d getItemPosition(ItemEntity itemEntity) {
        return new Vec3d(itemEntity.posX, itemEntity.posY, itemEntity.posZ);
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
                    // this is a special exception for item entities, because they look goofy when we push them while we're vacuuming
                    if (entity instanceof ItemEntity) {
                        continue;
                    }
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
        if (this.getWorld() == null) {
            return;
        }
        this.getBlockState().updateNeighbors(this.getWorld(), this.getPos(), 3);
    }

    public void openInventory(PlayerEntity player) {
        if (this.world == null) {
            return;
        }
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

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent(Names.ENDER_VACUUM_CONTAINER);
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
        if (!compound.contains(Names.TRACKED_ITEM_IDS)) {
            return;
        }
        ListNBT trackedIdNbt = compound.getList(Names.TRACKED_ITEM_IDS, 3); // list of ints;
        this.trackedItemIds.clear();
        for(int i = 0; i < trackedIdNbt.size(); i++) {
            int trackedId = trackedIdNbt.getInt(i);
            this.trackedItemIds.add(trackedId);
        }
    }

    public CompoundNBT saveToNbt(CompoundNBT compound) {
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.items, false);
        }
        ListNBT trackedIdNbt = new ListNBT();
        for (Integer trackedItemId : trackedItemIds) {
            IntNBT trackedId = new IntNBT(trackedItemId);
            trackedIdNbt.add(trackedId);
        }
        compound.put(Names.TRACKED_ITEM_IDS, trackedIdNbt);
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
