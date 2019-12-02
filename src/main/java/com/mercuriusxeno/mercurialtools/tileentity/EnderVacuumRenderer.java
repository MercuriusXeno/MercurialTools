package com.mercuriusxeno.mercurialtools.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ShulkerColorLayer;
import net.minecraft.client.renderer.entity.model.ShulkerModel;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnderVacuumRenderer  extends MobRenderer<ShulkerEntity, ShulkerModel<ShulkerEntity>> {
    public static final ResourceLocation field_204402_a = new ResourceLocation("mercurialtools/textures/entity/ender_vacuum.png");

    public EnderVacuumRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new ShulkerModel<>(), 0.0F);
    }

    public void doRender(ShulkerEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        int i = entity.getClientTeleportInterp();
        if (i > 0 && entity.isAttachedToBlock()) {
            BlockPos blockpos = entity.getAttachmentPos();
            BlockPos blockpos1 = entity.getOldAttachPos();
            double d0 = (double)((float)i - partialTicks) / 6.0D;
            d0 = d0 * d0;
            double d1 = (double)(blockpos.getX() - blockpos1.getX()) * d0;
            double d2 = (double)(blockpos.getY() - blockpos1.getY()) * d0;
            double d3 = (double)(blockpos.getZ() - blockpos1.getZ()) * d0;
            super.doRender(entity, x - d1, y - d2, z - d3, entityYaw, partialTicks);
        } else {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    public boolean shouldRender(ShulkerEntity livingEntity, ICamera camera, double camX, double camY, double camZ) {
        if (super.shouldRender(livingEntity, camera, camX, camY, camZ)) {
            return true;
        } else {
            if (livingEntity.getClientTeleportInterp() > 0 && livingEntity.isAttachedToBlock()) {
                BlockPos blockpos = livingEntity.getOldAttachPos();
                BlockPos blockpos1 = livingEntity.getAttachmentPos();
                Vec3d vec3d = new Vec3d((double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ());
                Vec3d vec3d1 = new Vec3d((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y, vec3d.z))) {
                    return true;
                }
            }

            return false;
        }
    }

    protected ResourceLocation getEntityTexture(ShulkerEntity entity) {
        return field_204402_a;
    }

    protected void applyRotations(ShulkerEntity entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
        switch(entityLiving.getAttachmentFacing()) {
            case DOWN:
            default:
                break;
            case EAST:
                GlStateManager.translatef(0.5F, 0.5F, 0.0F);
                GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
                break;
            case WEST:
                GlStateManager.translatef(-0.5F, 0.5F, 0.0F);
                GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                break;
            case NORTH:
                GlStateManager.translatef(0.0F, 0.5F, -0.5F);
                GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case SOUTH:
                GlStateManager.translatef(0.0F, 0.5F, 0.5F);
                GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
                break;
            case UP:
                GlStateManager.translatef(0.0F, 1.0F, 0.0F);
                GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
        }

    }

    protected void preRenderCallback(ShulkerEntity entitylivingbaseIn, float partialTickTime) {
        float f = 0.999F;
        GlStateManager.scalef(0.999F, 0.999F, 0.999F);
    }
}