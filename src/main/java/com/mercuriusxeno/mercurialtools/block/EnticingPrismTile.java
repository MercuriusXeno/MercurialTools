package com.mercuriusxeno.mercurialtools.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class EnticingPrismTile extends TileEntity implements ITickableTileEntity {
    public EnticingPrismTile() {
        super(ModBlocks.ENTICING_PRISM_TILE);
    }

    private AxisAlignedBB bobberField = null;
    private AxisAlignedBB getBobberField() {
        if (bobberField == null) {
            bobberField = new AxisAlignedBB(-4, 4, -4, 4, -4, 4);
        }
        return bobberField;
    }

    @Override
    public void tick() {
        if (this.world.isRemote()) {
            return;
        }

        // get entities that are a fishing bobber in a 9x9x9 cube centered around the lantern and tick them more than they're supposed to tick.
        List<FishingBobberEntity> bobbers = this.world.getEntitiesWithinAABB(FishingBobberEntity.class, getBobberField().offset(this.getPos()));
        for(FishingBobberEntity bobber : bobbers) {
            bobber.tick();
        }
    }
}
