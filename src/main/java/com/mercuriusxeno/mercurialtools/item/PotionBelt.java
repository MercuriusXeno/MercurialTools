package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.item.Item;

public class PotionBelt extends Item {
    public PotionBelt() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        setRegistryName(Names.POTION_BELT);
    }
}
