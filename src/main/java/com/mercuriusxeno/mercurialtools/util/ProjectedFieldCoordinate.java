package com.mercuriusxeno.mercurialtools.util;

import com.mercuriusxeno.mercurialtools.util.enums.AxisType;

public class ProjectedFieldCoordinate {
    public AxisType axis;

    public int coordinate;

    public ProjectedFieldCoordinate(AxisType axis, int coordinate) {
        this.axis = axis;
        this.coordinate = coordinate;
    }
}
