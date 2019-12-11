package com.mercuriusxeno.mercurialtools.reference;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModConstants {
    // interloper death field
    public static final int INTERLOPER_OFFSET_HEIGHT = 0;
    public static final int INTERLOPER_FIELD_HEIGHT = 5;
    public static final int INTERLOPER_OFFSET_WIDTH = 0;
    public static final int INTERLOPER_FIELD_WIDTH = 9;
    public static final int INTERLOPER_OFFSET_DEPTH = 0;
    public static final int INTERLOPER_FIELD_DEPTH = 9;

    public static final int ENDER_KEYSTONE_OFFSET_HEIGHT = 0;
    public static final int ENDER_KEYSTONE_FIELD_HEIGHT = 1;
    public static final int ENDER_KEYSTONE_OFFSET_WIDTH = 0;
    public static final int ENDER_KEYSTONE_FIELD_WIDTH = 1;
    public static final int ENDER_KEYSTONE_OFFSET_DEPTH = 0;
    public static final int ENDER_KEYSTONE_FIELD_DEPTH = 1;

    public static final double ENDER_KEYSTONE_WARP_FACTOR = 32D;
    public static final double ENDER_KEYSTONE_WARP_FACTOR_DAMPENED_I = ENDER_KEYSTONE_WARP_FACTOR / 2D;
    public static final double ENDER_KEYSTONE_WARP_FACTOR_DAMPENED_II = ENDER_KEYSTONE_WARP_FACTOR_DAMPENED_I / 2D;
    public static final double ENDER_KEYSTONE_WARP_FACTOR_DAMPENED_III = ENDER_KEYSTONE_WARP_FACTOR_DAMPENED_II / 2D;

    public static final int CRYSTAL_COMPASS_RANGE = 16;

    public static final double ENDER_VACUUM_RANGE = 8.0D;

    // approximation from the center of a cube to its corner, if the side length is 1 meter
    // the formula is (x * (Math.sqrt(3)) / 2) where x is the length of a side,
    // divided by 2 because I want the distance to the center, not the distance to the opposite corner.
    public static final double UNIT_CUBE_CORNER_DISTANCE_COEFFICIENT = 0.866D;
    public static final AxisAlignedBB EMPTY_BOUNDING_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    public static final long GROWTH_PULSER_CYCLE_TIME = 200;
}
