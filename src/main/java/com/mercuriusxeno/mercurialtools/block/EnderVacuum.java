package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.Block;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class EnderVacuum extends ContainerBlock {
    public EnderVacuum() {
        super(Block.Properties
                .create(Material.SHULKER)
                .sound(SoundType.STONE)
                .hardnessAndResistance(3.0f)
                .lightValue(0));
        setRegistryName(Names.ENDER_VACUUM);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return null;
    }
}
