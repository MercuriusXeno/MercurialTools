package com.mercuriusxeno.mercurialtools.reference;

import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;

public class Constants {
    public static final int INTERLOPER_UPDATE_COOLDOWN = 4;
    public static final int INTERLOPER_OFFSET_HEIGHT = 0;
    public static final int INTERLOPER_FIELD_HEIGHT = 5;
    public static final int INTERLOPER_OFFSET_WIDTH = 0;
    public static final int INTERLOPER_FIELD_WIDTH = 9;
    public static final int INTERLOPER_OFFSET_DEPTH = 0;
    public static final int INTERLOPER_FIELD_DEPTH = 9;

    public static final DirectionProperty TOPFACING = DirectionProperty.create("topfacing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);

}
