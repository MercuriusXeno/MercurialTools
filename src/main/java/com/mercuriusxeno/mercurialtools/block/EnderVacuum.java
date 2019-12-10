package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class EnderVacuum extends ContainerBlock {
    public EnderVacuum() {
        super(Block.Properties
                .create(Material.SHULKER)
                .sound(SoundType.STONE)
                .hardnessAndResistance(3.0f)
                .lightValue(0));
        setRegistryName(Names.ENDER_VACUUM);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP));
    }


    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
    public static final ResourceLocation resourceLocation = new ResourceLocation("contents");

    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new EnderVacuumTile();
    }

    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasCustomBreakingProgress(BlockState state) {
        return true;
    }

    /**
     * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return true;
        } else if (player.isSpectator()) {
            return true;
        } else {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof EnderVacuumTile) {
                Direction direction = state.get(FACING);
                EnderVacuumTile enderVacuumTileEntity = (EnderVacuumTile)tileentity;
                boolean flag;
                if (enderVacuumTileEntity.getAnimationStatus() == EnderVacuumTile.AnimationStatus.CLOSED) {
                    AxisAlignedBB axisalignedbb = VoxelShapes.fullCube().getBoundingBox().expand((double)(0.5F * (float)direction.getXOffset()), (double)(0.5F * (float)direction.getYOffset()), (double)(0.5F * (float)direction.getZOffset())).contract((double)direction.getXOffset(), (double)direction.getYOffset(), (double)direction.getZOffset());
                    flag = worldIn.areCollisionShapesEmpty(axisalignedbb.offset(pos.offset(direction)));
                } else {
                    flag = true;
                }

                if (flag) {
                    player.openContainer(enderVacuumTileEntity);
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getFace());
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public ItemStack getItemStack() {
        return new ItemStack(getBlock());
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
     * this block
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof EnderVacuumTile) {
            EnderVacuumTile enderVacuumTileEntity = (EnderVacuumTile)tileentity;
            if (!worldIn.isRemote && player.isCreative() && !enderVacuumTileEntity.isEmpty()) {
                ItemStack itemstack = getItemStack();
                CompoundNBT compoundnbt = enderVacuumTileEntity.saveToNbt(new CompoundNBT());
                if (!compoundnbt.isEmpty()) {
                    itemstack.setTagInfo("BlockEntityTag", compoundnbt);
                }

                if (enderVacuumTileEntity.hasCustomName()) {
                    itemstack.setDisplayName(enderVacuumTileEntity.getCustomName());
                }

                ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), itemstack);
                itementity.setDefaultPickupDelay();
                worldIn.addEntity(itementity);
            } else {
                enderVacuumTileEntity.fillWithLoot(player);
            }
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof EnderVacuumTile) {
            EnderVacuumTile enderVacuumTileEntity = (EnderVacuumTile)tileentity;
            builder = builder.withDynamicDrop(resourceLocation, (passedResourceLocation, lootContextProvider) -> {
                for(int i = 0; i < enderVacuumTileEntity.getSizeInventory(); ++i) {
                    lootContextProvider.accept(enderVacuumTileEntity.getStackInSlot(i));
                }

            });
        }

        return super.getDrops(state, builder);
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasDisplayName()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof EnderVacuumTile) {
                ((EnderVacuumTile)tileentity).setCustomName(stack.getDisplayName());
            }
        }

    }

    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof EnderVacuumTile) {
                worldIn.updateComparatorOutputLevel(pos, state.getBlock());
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("LootTable", 8)) {
                tooltip.add(new StringTextComponent("???????"));
            }

            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                int i = 0;
                int j = 0;

                for(ItemStack itemstack : nonnulllist) {
                    if (!itemstack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            ITextComponent itextcomponent = itemstack.getDisplayName().deepCopy();
                            itextcomponent.appendText(" x").appendText(String.valueOf(itemstack.getCount()));
                            tooltip.add(itextcomponent);
                        }
                    }
                }

                if (j - i > 0) {
                    tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).applyTextStyle(TextFormatting.ITALIC));
                }
            }
        }

    }

    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof EnderVacuumTile ? VoxelShapes.create(((EnderVacuumTile)tileentity).getBoundingBox(state)) : VoxelShapes.fullCube();
    }

    public boolean isSolid(BlockState state) {
        return false;
    }

    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        return Container.calcRedstoneFromInventory((IInventory)worldIn.getTileEntity(pos));
    }

    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getItem(worldIn, pos, state);
        EnderVacuumTile shulkerboxtileentity = (EnderVacuumTile)worldIn.getTileEntity(pos);
        CompoundNBT compoundnbt = shulkerboxtileentity.saveToNbt(new CompoundNBT());
        if (!compoundnbt.isEmpty()) {
            itemstack.setTagInfo("BlockEntityTag", compoundnbt);
        }

        return itemstack;
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
}
