package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.ModConstants;
import com.mercuriusxeno.mercurialtools.util.ModState;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class GrowthPulserTile extends TileEntity implements ITickableTileEntity {
    public GrowthPulserTile() {
        super(ModBlocks.GROWTH_PULSER_TILE);
    }

    private AxisAlignedBB growthField = null;
    private AxisAlignedBB getGrowthField() {
        if (growthField == null) {
            growthField = new AxisAlignedBB(-4, 4, -4, 4, -4, 4);
        }
        return growthField;
    }

    @Override
    public void tick() {
        if (this.world.isRemote()) {
            return;
        }

        if (this.world.getGameTime() % ModConstants.GROWTH_PULSER_CYCLE_TIME > 0) {
            return;
        }

        long scheduledTick = getNextValidGrowthScheduleTick();
        if (scheduledTick == 0) {
            return;
        }

        // get blocks that are crops of some kind and tick them more than they're supposed to tick.
        AxisAlignedBB thisBb = getGrowthField().offset(this.getPos());
        for(int x = (int)Math.floor(thisBb.minX); x <= (int)Math.floor(thisBb.maxX); x++) {
            for(int y = (int)Math.floor(thisBb.minY); y <= (int)Math.floor(thisBb.maxY); y++) {
                for(int z = (int)Math.floor(thisBb.minZ); z <= (int)Math.floor(thisBb.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = this.world.getBlockState(pos);
                    if (state.getBlock() instanceof IGrowable) {
                        if (MercurialTools.state.isGrowthPulseScheduled(this.world, pos, scheduledTick)) {
                            continue;
                        }
                        MercurialTools.state.scheduleGrowthPulse(this.world, pos, scheduledTick);
                    }
                }
            }
        }

        MercurialTools.state.handleGrowthPulses(this.world);
    }

    private long getNextValidGrowthScheduleTick() {
        // you shouldn't be here if the world is null or client sided.
        if (this.getWorld() == null || this.getWorld().isRemote()) {
            return 0;
        }

        // if the next scheduled tick is "now", defer it to the next 10 second increment.
        if (this.getWorld().getGameTime() % ModConstants.GROWTH_PULSER_CYCLE_TIME == 0) {
            return this.getWorld().getGameTime() + ModConstants.GROWTH_PULSER_CYCLE_TIME;
        }

        // the time to add is the remainder of the cycle time modulo of current time subtracted from the cycle time
        long timeToAdd = (ModConstants.GROWTH_PULSER_CYCLE_TIME - (this.getWorld().getGameTime() % ModConstants.GROWTH_PULSER_CYCLE_TIME));
        return timeToAdd + this.getWorld().getGameTime();
    }
}
