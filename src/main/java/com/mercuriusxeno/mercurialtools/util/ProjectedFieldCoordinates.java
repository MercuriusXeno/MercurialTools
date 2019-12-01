package com.mercuriusxeno.mercurialtools.util;

import com.mercuriusxeno.mercurialtools.util.enums.AxisType;

public class ProjectedFieldCoordinates {
    ProjectedFieldCoordinate[] coordinates = new ProjectedFieldCoordinate[3];
    public ProjectedFieldCoordinates(int x, int y, int z) {
        ProjectedFieldCoordinate xCoord = new ProjectedFieldCoordinate(AxisType.X, x);
        ProjectedFieldCoordinate yCoord = new ProjectedFieldCoordinate(AxisType.Y, y);
        ProjectedFieldCoordinate zCoord = new ProjectedFieldCoordinate(AxisType.Z, z);
        coordinates[0] = xCoord;
        coordinates[1] = yCoord;
        coordinates[2] = zCoord;
    }
    public int getAxisCoordinate(AxisType axis) {
        switch(axis) {
            case X:
                return coordinates[0].coordinate;
            case Y:
                return coordinates[1].coordinate;
            case Z:
                return coordinates[2].coordinate;
        }
        return 0;
    }

    public int getX() {
        return getAxisCoordinate(AxisType.X);
    }

    public int getY() {
        return getAxisCoordinate(AxisType.Y);
    }

    public int getZ() {
        return getAxisCoordinate(AxisType.Z);
    }
}
