package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.item.Item;

public class MercurialManual extends Item {
    public MercurialManual() {
        super(new Item.Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        setRegistryName(Names.MERCURIAL_MANUAL);
    }
}
