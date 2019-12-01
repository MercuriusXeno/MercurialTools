package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.Constants;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.AlignedField;
import com.mercuriusxeno.mercurialtools.util.ProjectedField;
import com.mercuriusxeno.mercurialtools.util.ProjectedFieldCoordinates;
import com.mercuriusxeno.mercurialtools.util.enums.AlignmentBias;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Interloper extends DirectionalBlock {
    public static final ProjectedField DEATHFIELD =
            new ProjectedField(AlignmentBias.CENTER, AlignmentBias.CENTER, AlignmentBias.NEGATIVE,
                    Constants.INTERLOPER_OFFSET_HEIGHT, Constants.INTERLOPER_FIELD_HEIGHT,
                    Constants.INTERLOPER_OFFSET_WIDTH, Constants.INTERLOPER_FIELD_WIDTH,
                    Constants.INTERLOPER_OFFSET_DEPTH, Constants.INTERLOPER_FIELD_DEPTH);

    public static final DirectionProperty TOPFACING = Constants.TOPFACING;

    public Interloper() {
        super(Block.Properties
                .create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(4.0f)
                .lightValue(0));
        setRegistryName(Names.INTERLOPER);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH).with(TOPFACING, Direction.UP));
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, TOPFACING);
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
        Direction topDirection = state.get(TOPFACING);

        AlignedField deathField = DEATHFIELD.getProjectedField(pos, faceDirection, topDirection);

        AxisAlignedBB boundingBox = deathField.getBoundingBox();

        List<LivingEntity> entities = worldIn.getEntitiesWithinAABB(LivingEntity.class, boundingBox, e -> !(e instanceof PlayerEntity));
        for(LivingEntity e : entities) {
            e.onDeath(DamageSource.GENERIC);
        }

        // check if there are any living entities that aren't players in the area.
        // make sure those entities are whitelisted for murder via interloper.

        // schedule the next tick
        worldIn.getPendingBlockTicks().scheduleTick(pos, this, Constants.INTERLOPER_UPDATE_COOLDOWN);
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
        return this.getDefaultState().with(FACING, facing).with(TOPFACING, topFacing);
    }
}
