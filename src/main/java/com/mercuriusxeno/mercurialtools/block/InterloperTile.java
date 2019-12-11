package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.ModConstants;
import com.mercuriusxeno.mercurialtools.util.AlignedField;
import com.mercuriusxeno.mercurialtools.util.ProjectedField;
import com.mercuriusxeno.mercurialtools.util.enums.AlignmentBias;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class InterloperTile extends TileEntity implements ITickableTileEntity {
    public InterloperTile() {
        super(ModBlocks.INTERLOPER_TILE);
    }

    private static final ProjectedField DEATHFIELD_AREA =
            new ProjectedField(AlignmentBias.CENTER, AlignmentBias.CENTER, AlignmentBias.NEGATIVE,
                    ModConstants.INTERLOPER_OFFSET_HEIGHT, ModConstants.INTERLOPER_FIELD_HEIGHT,
                    ModConstants.INTERLOPER_OFFSET_WIDTH, ModConstants.INTERLOPER_FIELD_WIDTH,
                    ModConstants.INTERLOPER_OFFSET_DEPTH, ModConstants.INTERLOPER_FIELD_DEPTH);


    private AlignedField deathField = null;
    private AlignedField getDeathField() {
        if (deathField == null) {
            // offset the projected field forward (by facing) one block so that it aligns with the face and not the back.
            deathField = DEATHFIELD_AREA
                    .getProjectedField(
                            this.pos.offset(this.getBlockState().get(Interloper.FACING)),
                            this.getBlockState().get(Interloper.FACING),
                            this.getBlockState().get(Interloper.TOPFACING)
                    );
        }
        return deathField;
    }

    private AxisAlignedBB deathBox = null;
    private AxisAlignedBB getDeathBox() {
        if (deathBox == null) {
            deathBox = getDeathField().getBoundingBox();
        }
        return deathBox;
    }

    public void tick() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        List<LivingEntity> entities = this.world.getEntitiesWithinAABB(LivingEntity.class, getDeathBox(), e -> !(e instanceof PlayerEntity) && e.isNonBoss());
        for(LivingEntity e : entities) {
            if (!this.getBlockState().get(Interloper.POWERED)) {
                if (e.getHealth() > 0.5F) {
                    e.setHealth(0.5F);
                }
            } else {
                e.setHealth(0F);
                e.onDeath(DamageSource.GENERIC);
            }
        }
    }
}
