package com.mercuriusxeno.mercurialtools.blocks;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class SpawnerTemplate extends Block {
    public SpawnerTemplate() {
        super(Properties
                .create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(4.0f)
                .lightValue(0));
        setRegistryName(Names.SPAWNER_TEMPLATE);
    }
}
