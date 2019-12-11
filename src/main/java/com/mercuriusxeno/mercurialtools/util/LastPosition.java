package com.mercuriusxeno.mercurialtools.util;


import com.mercuriusxeno.mercurialtools.reference.ModConstants;
import net.minecraft.util.math.Vec3d;

/**
 * Used by Ender Keystones to help facilitate teleportation stuff, and be more awesome.
 */
public class LastPosition {

    public LastPosition(Vec3d lastPosition, int entityId, long tickFor) {
        this.lastPosition = lastPosition;
        this.entityId = entityId;
        this.tickFor = tickFor;
    }
    public Vec3d lastPosition;
    public int entityId;
    public long tickFor;

    public Vec3d getMovementVector(Vec3d currentPosition, double warpFactor) {
        Vec3d originalVector = (currentPosition.subtract(lastPosition)).normalize();
        // create a cardinal vector so that we don't get weird diagonal movements, because diagonals are annoying to control.
        double absX = Math.abs(originalVector.x);
        double absY = Math.abs(originalVector.y);
        double absZ = Math.abs(originalVector.z);
        if (absX > absY && absX > absZ) {
            // x is dominant, do X
            return new Vec3d(originalVector.x, 0.0D, 0.0D).normalize().scale(warpFactor);
        } else if (absZ > absY) {
            return new Vec3d(0.0D, 0.0D, originalVector.z).normalize().scale(warpFactor);
        } else {
            return new Vec3d(0.0D, originalVector.y, 0.0D).normalize().scale(warpFactor);
        }
    }
}
