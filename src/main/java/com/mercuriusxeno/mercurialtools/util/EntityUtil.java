package com.mercuriusxeno.mercurialtools.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.List;

public class EntityUtil {
    public static LivingEntity getClosestEntityToPlayer(World worldIn, PlayerEntity playerIn, double radius) {
        List<LivingEntity> entities =  worldIn.getEntitiesWithinAABB(
                LivingEntity.class,
                playerIn.getBoundingBox().grow(radius, radius, radius),
                (entity) -> entity != playerIn && entity.isAlive()
        );

        double closestEntityDistance = Double.MAX_VALUE;
        LivingEntity closestEntity = null;
        for(LivingEntity e : entities) {
            // skip players for reasons that should be obvious
            if (e instanceof PlayerEntity) {
                continue;
            }
            double entityDistance = e.getDistance(playerIn);
            if (entityDistance > radius) {
                continue;
            }

            if (entityDistance < closestEntityDistance) {
                closestEntity = (LivingEntity)e;
                closestEntityDistance = entityDistance;
            }
        }

        return closestEntity;
    }
}
