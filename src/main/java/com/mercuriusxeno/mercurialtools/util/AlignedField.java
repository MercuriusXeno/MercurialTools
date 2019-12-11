package com.mercuriusxeno.mercurialtools.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

// I have a tendency to project fields a lot. A projected field is just a zone of control
// where I want to perform some iterative operation on blocks, or a zone to define an AABB to
// capture entities within. This is a utility class to help me do that
public class AlignedField {
    // the direction of the projected field, used to determine its actual axis boundaries
    public Direction faceDirection;

    // the orientation of the "top" of the projected field, mostly only relevant when the block has an orientation where up and downward facing can rotate around the Y axis
    public Direction topDirection;

    // the height, width and depth of each axis of the projected field, in the negative direction.
    public ProjectedFieldCoordinates negativeBoundaries;

    // the height, width and depth of each axis of the projected field, in the positive direction.
    public ProjectedFieldCoordinates positiveBoundaries;

    public BlockPos position;

    public AlignedField(BlockPos position, Direction faceDirection, Direction topDirection, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax) {
        this.position = position;
        this.negativeBoundaries = new ProjectedFieldCoordinates(xMin, yMin, zMin);
        this.positiveBoundaries = new ProjectedFieldCoordinates(xMax, yMax, zMax);
        this.faceDirection = faceDirection;
        this.topDirection = topDirection;
    }

    public List<ProjectedFieldCoordinates> getFieldCoordinatesAsList() {
        ArrayList<ProjectedFieldCoordinates> coords = new ArrayList<>();

        for (int x = negativeBoundaries.getX(); x <= positiveBoundaries.getX(); x++) {
            for (int y = negativeBoundaries.getY(); y <= positiveBoundaries.getY(); y++) {
                for (int z = negativeBoundaries.getZ(); z <= positiveBoundaries.getZ(); z++) {
                    coords.add(new ProjectedFieldCoordinates(x, y, z));
                }
            }
        }

        return coords;
    }

    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(
                this.negativeBoundaries.getX(),
                this.negativeBoundaries.getY(),
                this.negativeBoundaries.getZ(),
                this.positiveBoundaries.getX(),
                this.positiveBoundaries.getY(),
                this.positiveBoundaries.getZ()).offset(this.position);
    }
}
