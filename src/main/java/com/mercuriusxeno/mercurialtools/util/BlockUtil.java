package com.mercuriusxeno.mercurialtools.util;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.conditions.BlockStateProperty;

public class BlockUtil {
    public static final ResourceLocation dampenedPropertyResourceLocation = new ResourceLocation(Names.MOD_ID, Names.DAMPENING_LEVEL);
    public static final IntegerProperty dampenedProperty = IntegerProperty.create(dampenedPropertyResourceLocation.getPath(), 0, 3);
}
