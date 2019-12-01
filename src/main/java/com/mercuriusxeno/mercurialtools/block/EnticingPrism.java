package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class EnticingPrism extends Block {
    public EnticingPrism() {
        super(Block.Properties
                .create(Material.GLASS)
                .sound(SoundType.GLASS)
                .hardnessAndResistance(2.0f)
                .lightValue(15));
        setRegistryName(Names.ENTICING_PRISM);
    }
}
