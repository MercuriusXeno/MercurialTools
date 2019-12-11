package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.ModConstants;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.AlignedField;
import com.mercuriusxeno.mercurialtools.util.BlockUtil;
import com.mercuriusxeno.mercurialtools.util.ProjectedField;
import com.mercuriusxeno.mercurialtools.util.enums.AlignmentBias;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class EnderKeystone extends DirectionalBlock {

    public EnderKeystone() {
        super(Block.Properties
                .create(Material.ROCK)
                .sound(SoundType.STONE)
                .hardnessAndResistance(3.0f)
                .lightValue(0));
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH).with(BlockUtil.dampenedProperty, 0));
    }
    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getBlock().equals(this);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnderKeystoneTile();
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(BlockUtil.dampenedProperty);
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

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        ItemStack stack = context.getItem();
        // the keystone's placement "front" is toward the player instead of away.
        Direction facing = context.getNearestLookingDirection().getOpposite();
        if (stack.getItem().equals(ModBlocks.ENDER_KEYSTONE_DAMPENED_I.asItem())) {
            return this.getDefaultState().with(FACING, facing).with(BlockUtil.dampenedProperty, 1);
        } else if(stack.getItem().equals(ModBlocks.ENDER_KEYSTONE_DAMPENED_II.asItem())) {
            return this.getDefaultState().with(FACING, facing).with(BlockUtil.dampenedProperty, 2);
        } else if(stack.getItem().equals(ModBlocks.ENDER_KEYSTONE_DAMPENED_III.asItem())) {
            return this.getDefaultState().with(FACING, facing).with(BlockUtil.dampenedProperty, 3);
        }
        return this.getDefaultState().with(FACING, facing);
    }
}
