package com.mercuriusxeno.mercurialtools.util;

import com.mercuriusxeno.mercurialtools.util.enums.AlignmentBias;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;

// class representing a projected field with behaviors and dimensions, and offsets.
// when the field definition is requested for dimensions for a particular position and direction/top-cardinality, it
// uses its values to produce them relative to the origin, with its behaviors respected.
public class ProjectedField {

    // alignment styles control the orientation of the projected field in various ways.

    // whether the field is vertically aligned with the top of the block, the bottom of the block, or centered on the face of the block.
    public AlignmentBias verticalAlignment;
    // whether the field is horizontally aligned with either side of the block, or centered on the face of the block.
    public AlignmentBias horizontalAlignment;
    // whether the field is aligned [in depth] to the front of the block, the back of it, or centered over it.
    public AlignmentBias perpendicularAlignment;

    // dimensions control the size of the field, strictly speaking odd-sizes are "best", even sizes are off by 1 in the positive direction, which is weird, but allowed.

    // the tallness (y length) of the field.
    public int height;
    // the width of the entire field, in directions perpendicular, laterally, to its face.
    public int width;
    // the depth of the entire field, in the direction of its face.
    public int depth;

    // offsets control the shift in dimension relative to each axis, used to push or pull the field relative to its origin in a given direction.

    // the offset up or down of the entire field from its projecting origin.
    public int heightOffset;
    // the offset left or right of the entire field relative to the face.
    public int widthOffset;
    // the offset backwards or forwards of the entire field relative to the face.
    public int depthOffset;

    public ProjectedField(AlignmentBias verticalAlignment,
                          AlignmentBias horizontalAlignment,
                          AlignmentBias perpendicularAlignment,
                          int heightOffset, int height,
                          int widthOffset, int width,
                          int depthOffset, int depth) {
        this.verticalAlignment = verticalAlignment;
        this.horizontalAlignment = horizontalAlignment;
        this.perpendicularAlignment = perpendicularAlignment;

        // set dimensions
        this.height = height;
        this.depth = depth;
        this.width = width;

        // set offsets
        this.heightOffset = heightOffset;
        this.widthOffset = widthOffset;
        this.depthOffset = depthOffset;
    }

    public AlignedField getProjectedField(BlockPos position, Direction faceDirection, Direction topDirection) {
        // the price of these lengths being even is that an even length places it a block offset from an otherwise central position, always in the positive direction.
        Tuple<Integer, Integer> widthMagnitudes = getMagnitudes(width, widthOffset, horizontalAlignment,
                isWidthBiasInverted(faceDirection, topDirection));
        Tuple<Integer, Integer> heightMagnitudes = getMagnitudes(height, heightOffset, verticalAlignment, isNegativeAxis(topDirection));
        Tuple<Integer, Integer> depthMagnitudes = getMagnitudes(depth, depthOffset, perpendicularAlignment, isNegativeAxis(faceDirection));

        // assemble boundaries based on direction and dimensions.
        int xMin = 0;
        int xMax = 0;
        int yMin = 0;
        int yMax = 0;
        int zMin = 0;
        int zMax = 0;

        // the ugly part, switch case over directional facings and respect top-side cardinality when facing up or down.
        // this determines what axis gets the "wide", "deep" or "high" params, as well as how offsets and centering apply.

        switch(faceDirection) {
            case UP:
            case DOWN:
                yMin = depthMagnitudes.getA();
                yMax = depthMagnitudes.getB();
                switch(topDirection) {
                    case NORTH:
                    case SOUTH:
                        xMin = widthMagnitudes.getA();
                        xMax = widthMagnitudes.getB();
                        zMin = heightMagnitudes.getA();
                        zMax = heightMagnitudes.getB();
                        break;
                    case EAST:
                    case WEST:
                        xMin = heightMagnitudes.getA();
                        xMax = heightMagnitudes.getB();
                        zMin = widthMagnitudes.getA();
                        zMax = widthMagnitudes.getB();
                        break;
                }
                break;
            case NORTH:
            case SOUTH:
                xMin = widthMagnitudes.getA();
                xMax = widthMagnitudes.getB();
                yMin = heightMagnitudes.getA();
                yMax = heightMagnitudes.getB();
                zMin = depthMagnitudes.getA();
                zMax = depthMagnitudes.getB();
                break;
            case WEST:
            case EAST:
                xMin = depthMagnitudes.getA();
                xMax = depthMagnitudes.getB();
                yMin = heightMagnitudes.getA();
                yMax = heightMagnitudes.getB();
                zMin = widthMagnitudes.getA();
                zMax = widthMagnitudes.getB();
                break;
        }
        return new AlignedField(position, faceDirection, topDirection, xMin, xMax, yMin, yMax, zMin, zMax);
    }

    private static boolean isWidthBiasInverted(Direction faceDirection, Direction topDirection) {
        return isFacingNegativeLefternBias(faceDirection) || (isFacingDownOrUp(faceDirection) && isFacingNegativeLefternBias(topDirection));
    }

    private static boolean isFacingNegativeLefternBias(Direction direction) {
        return direction == Direction.EAST || direction == Direction.NORTH;
    }

    private static boolean isNegativeAxis(Direction direction) {
        return direction == Direction.WEST || direction == Direction.NORTH || direction == Direction.DOWN;
    }

    private static boolean isFacingDownOrUp(Direction direction) {
        return direction == Direction.UP || direction == Direction.DOWN;
    }

    private static Tuple<Integer, Integer> getMagnitudes(int length, int offset, AlignmentBias bias, boolean isInvertedDominance) {
        if (length <= 0) {
            throw new IllegalArgumentException("Projected field instantiated with a dimension of 0 or less. That's not how math works pal.");
        }

        int heightOffsetCoefficient = isInvertedDominance ? -1 : 1;

        // presume that a length of 1 is essentially a length of 0.
        // if a block projects 1 block deep from its back, it's projecting the column it exists in.
        // if a block projects 1 block wide from its negative side, its projecting the column it exists in, and so on.
        // therefore, a block projecting 2 blocks [in some direction] should project slightly positive (left, forward, or up, depending on orientation)
        int oddCompensation =  (length % 2);
        int evenCompensation = 1 - oddCompensation;
        int centerishPoint = (length + oddCompensation) / 2;
        // trend positive to observe that ordinal 0 is a position and not nothing, but only when the number is even.
        Integer negativeMagnitude = (centerishPoint - length) + evenCompensation;
        Integer positiveMagnitude = (length - centerishPoint);

        // now we observe bias in the offsets.
        // a magnitude with a "negative bias" is one that favors projecting on its positive side (it's aligned/flush with its negative facing side)
        // the bias offset "pushes" the field in the direction based on the polarity of the direction (inverted dominance) and the bias specified.
        // a "centered" offset is the standard offset, so there is no bias.
        int biasOffset = 0;
        switch(bias) {
            case POSITIVE:
                biasOffset = (-positiveMagnitude) * heightOffsetCoefficient;
                break;
            case NEGATIVE:
                biasOffset = (-negativeMagnitude) * heightOffsetCoefficient;
                break;
            case CENTER:
                biasOffset = 0;
                break;
        }

        // summarize the offsets with the magnitudes as a tuple and pass it back as lower and upper bounds of a single axis.
        return new Tuple<>(negativeMagnitude + offset + biasOffset, positiveMagnitude + offset + biasOffset);
    }
}
