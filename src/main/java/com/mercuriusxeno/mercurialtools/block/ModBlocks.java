package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.container.MercurialGrindstoneContainer;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlocks {
    @ObjectHolder("mercurialtools:condensing_hopper")
    public static CondensingHopper CONDENSING_HOPPER;

    @ObjectHolder("mercurialtools:condensing_hopper")
    public static TileEntityType<CondensingHopperTile> CONDENSING_HOPPER_TILE;

    @ObjectHolder("mercurialtools:ender_keystone")
    public static EnderKeystone ENDER_KEYSTONE;

    @ObjectHolder("mercurialtools:ender_keystone_dampened_i")
    public static EnderKeystone ENDER_KEYSTONE_DAMPENED_I;

    @ObjectHolder("mercurialtools:ender_keystone_dampened_ii")
    public static EnderKeystone ENDER_KEYSTONE_DAMPENED_II;

    @ObjectHolder("mercurialtools:ender_keystone_dampened_iii")
    public static EnderKeystone ENDER_KEYSTONE_DAMPENED_III;

    @ObjectHolder("mercurialtools:ender_keystone")
    public static TileEntityType<EnderKeystoneTile> ENDER_KEYSTONE_TILE;

    @ObjectHolder("mercurialtools:ender_vacuum")
    public static EnderVacuum ENDER_VACUUM;

    @ObjectHolder("mercurialtools:ender_vacuum")
    public static TileEntityType<EnderVacuumTile> ENDER_VACUUM_TILE;

    @ObjectHolder("mercurialtools:enticing_prism")
    public static EnticingPrism ENTICING_PRISM;

    @ObjectHolder("mercurialtools:enticing_prism")
    public static TileEntityType<EnticingPrismTile> ENTICING_PRISM_TILE;

    @ObjectHolder("mercurialtools:expanding_hopper")
    public static ExpandingHopper EXPANDING_HOPPER;

    @ObjectHolder("mercurialtools:expanding_hopper")
    public static TileEntityType<ExpandingHopperTile> EXPANDING_HOPPER_TILE;

    @ObjectHolder("mercurialtools:growth_pulser")
    public static GrowthPulser GROWTH_PULSER;

    @ObjectHolder("mercurialtools:growth_pulser")
    public static TileEntityType<GrowthPulserTile> GROWTH_PULSER_TILE;

    @ObjectHolder("mercurialtools:interloper")
    public static Interloper INTERLOPER;

    @ObjectHolder("mercurialtools:interloper")
    public static TileEntityType<InterloperTile> INTERLOPER_TILE;

    @ObjectHolder("mercurialtools:mercurial_grindstone")
    public static MercurialGrindstone MERCURIAL_GRINDSTONE;

    @ObjectHolder("mercurialtools:mercurial_grindstone")
    public static ContainerType<MercurialGrindstoneContainer> MERCURIAL_GRINDSTONE_CONTAINER;

    @ObjectHolder("mercurialtools:spawner_template")
    public static SpawnerTemplate SPAWNER_TEMPLATE;
}
