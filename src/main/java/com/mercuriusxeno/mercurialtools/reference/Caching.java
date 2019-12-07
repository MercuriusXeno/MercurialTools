package com.mercuriusxeno.mercurialtools.reference;

import net.minecraft.util.math.AxisAlignedBB;

public class Caching {
    public static final AxisAlignedBB EMPTY_BOUNDING_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);;
    public static AxisAlignedBB getEmptyBoundingBox() {
        return EMPTY_BOUNDING_BOX;
    }
}
