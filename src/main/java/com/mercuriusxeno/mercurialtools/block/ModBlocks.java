package com.mercuriusxeno.mercurialtools.block;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlocks {
    @ObjectHolder("mercurialtools:condensing_hopper")
    public static CondensingHopper CONDENSING_HOPPER;

    @ObjectHolder("mercurialtools:condensing_hopper")
    public static TileEntityType<CondensingHopperTile> CONDENSING_HOPPER_TILE;

    @ObjectHolder("mercurialtools:ender_keystone")
    public static EnderKeystone ENDER_KEYSTONE;

    @ObjectHolder("mercurialtools:ender_vacuum")
    public static EnderVacuum ENDER_VACUUM;

    @ObjectHolder("mercurialtools:ender_vacuum")
    public static TileEntityType<EnderVacuumTile> ENDER_VACUUM_TILE;

    @ObjectHolder("mercurialtools:enticing_prism")
    public static EnticingPrism ENTICING_PRISM;

    @ObjectHolder("mercurialtools:expanding_hopper")
    public static ExpandingHopper EXPANDING_HOPPER;

    @ObjectHolder("mercurialtools:expanding_hopper")
    public static TileEntityType<ExpandingHopperTile> EXPANDING_HOPPER_TILE;

    @ObjectHolder("mercurialtools:growth_pulser")
    public static GrowthPulser GROWTH_PULSER;

    @ObjectHolder("mercurialtools:interloper")
    public static Interloper INTERLOPER;

    @ObjectHolder("mercurialtools:spawner_template")
    public static SpawnerTemplate SPAWNER_TEMPLATE;
}
