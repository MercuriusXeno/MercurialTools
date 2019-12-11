package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class Interloper extends DirectionalBlock {

    public static final DirectionProperty TOPFACING = DirectionProperty.create("topfacing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public Interloper() {
        super(Block.Properties
                .create(Material.ROCK)
                .sound(SoundType.STONE)
                .hardnessAndResistance(3.0f)
                .lightValue(0));
        setRegistryName(Names.INTERLOPER);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH).with(TOPFACING, Direction.UP).with(POWERED, false));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getBlock().equals(this);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return state.getBlock().equals(this) ? new InterloperTile() : null;
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, TOPFACING, POWERED);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return state.get(FACING) == side;
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

    protected boolean shouldBeOn(IWorld worldIn, BlockPos pos, BlockState state) {
        if (worldIn instanceof World) {
            Direction facing = state.get(FACING);
            return ((World)worldIn).isSidePowered(pos, facing.getOpposite());
        }
        return false;
    }

    public static void update(BlockState state, IWorld worldIn, BlockPos pos, boolean isPowered) {
        if (state.get(POWERED)) {
            if (!isPowered) {
                worldIn.setBlockState(pos, state.with(POWERED, Boolean.FALSE), Constants.BlockFlags.DEFAULT);
            }
        } else if (isPowered) {
            worldIn.setBlockState(pos, state.with(POWERED, Boolean.TRUE), Constants.BlockFlags.DEFAULT);
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!worldIn.isRemote()) {
            update(stateIn, worldIn, currentPos, shouldBeOn(worldIn, currentPos, stateIn));
        }

        return stateIn.with(POWERED, this.shouldBeOn(worldIn, currentPos, stateIn));
    }

    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote()) {
            update(state, worldIn, pos, shouldBeOn(worldIn, pos, state));
        }
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        // unlike the observer the interloper can rotate on its Y axis, based on the side hit.
        Direction facing = context.getNearestLookingDirection();
        Direction topFacing = Direction.UP;
        if (facing == Direction.UP || facing == Direction.DOWN) {
            Direction[] allFacings = context.getNearestLookingDirections();
            for (Direction aFacing : allFacings) {
                if (aFacing == facing) {
                    continue;
                }

                topFacing = aFacing;
                break;
            }
        }
        return this.getDefaultState().with(FACING, facing).with(TOPFACING, topFacing).with(POWERED, shouldBeOn(context.getWorld(), context.getPos(), this.getDefaultState().with(FACING, facing).with(TOPFACING, topFacing)));
    }
}
