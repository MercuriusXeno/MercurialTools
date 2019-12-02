package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.item.Item;

public class MercurialBlend extends Item {
    public MercurialBlend() {
        super(new Item.Properties()
                .maxStackSize(64)
                .group(MercurialTools.setup.itemGroup));
        setRegistryName(Names.MERCURIAL_BLEND);
    }
}
