package com.mercuriusxeno.mercurialtools.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;

public class ModState {
    public class GrowthTickScheduled {
        public World world;
        public BlockPos position;
        public long scheduledTick;

        public GrowthTickScheduled(World world, BlockPos position, long scheduledTick) {
            this.world = world;
            this.position = position;
            this.scheduledTick = scheduledTick;
        }
    }

    // these aren't saved on world closures, it's ten seconds, with loaded chunks only. I could save it, but I don't want to.
    public ArrayList<GrowthTickScheduled> growthTicksScheduled = new ArrayList<>();
    public ArrayList<GrowthTickScheduled> growthTicksRemoved = new ArrayList<>();
    public boolean isGrowthPulseScheduled(World world, BlockPos pos, long scheduledTick) {
        for(GrowthTickScheduled tick : growthTicksScheduled) {
            if (tick.world.getDimension().equals(world.getDimension()) && tick.position.equals(pos) && tick.scheduledTick == scheduledTick) {
                return true;
            }
        }

        return false;
    }

    public void scheduleGrowthPulse(World world, BlockPos pos, long scheduledTick) {
        growthTicksScheduled.add(new GrowthTickScheduled(world, pos, scheduledTick));
    }

    public void handleGrowthPulses(World world) {
        for(GrowthTickScheduled t : growthTicksScheduled) {
            if (!t.world.getDimension().equals(world.getDimension())) {
                continue;
            }
            if (!t.world.isAreaLoaded(t.position, 1)) {
                continue;
            }
            if (t.world.getGameTime() < t.scheduledTick) {
                continue;
            }
            BlockState state = t.world.getBlockState(t.position);
            if ((state.getBlock() instanceof IGrowable)) {
                handleGrowthPulse(t, (IGrowable)state.getBlock());
            }
            removeGrowthPulse(t);
        }
        for(GrowthTickScheduled r : growthTicksRemoved) {
            growthTicksScheduled.removeIf(t -> t.equals(r));
        }
    }

    private void removeGrowthPulse(GrowthTickScheduled t) {
        growthTicksRemoved.add(t);
    }

    public void handleGrowthPulse(GrowthTickScheduled scheduledTickInfo, IGrowable growableBlock) {
        growableBlock.grow(scheduledTickInfo.world, scheduledTickInfo.world.rand, scheduledTickInfo.position, scheduledTickInfo.world.getBlockState(scheduledTickInfo.position));
        scheduledTickInfo.world.playEvent(Constants.WorldEvents.BONEMEAL_PARTICLES, scheduledTickInfo.position.offset(Direction.UP), 1);
    }
}
