package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class GrowthPulser extends Block {
    public GrowthPulser() {
        super(Block.Properties
                .create(Material.GLASS)
                .sound(SoundType.GLASS)
                .hardnessAndResistance(2.0f)
                .lightValue(15));
        setRegistryName(Names.GROWTH_PULSER);
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getBlock().equals(this);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return state.getBlock().equals(this) ? new GrowthPulserTile() : null;
    }
}
