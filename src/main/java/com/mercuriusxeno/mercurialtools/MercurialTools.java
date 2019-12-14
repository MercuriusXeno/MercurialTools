package com.mercuriusxeno.mercurialtools;

import com.mercuriusxeno.mercurialtools.block.*;
import com.mercuriusxeno.mercurialtools.container.MercurialGrindstoneContainer;
import com.mercuriusxeno.mercurialtools.item.*;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.setup.ClientProxy;
import com.mercuriusxeno.mercurialtools.setup.IProxy;
import com.mercuriusxeno.mercurialtools.setup.ModSetup;
import com.mercuriusxeno.mercurialtools.setup.ServerProxy;
import com.mercuriusxeno.mercurialtools.util.ModState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("mercurialtools")
public class MercurialTools
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static ModSetup setup = new ModSetup();

    public static ModState state = new ModState();

    public static IProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

    // TODO ALWAYS DISABLE THIS FOR RELEASES
    public static boolean isDebug = true;

    public MercurialTools() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        // FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        // FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("Mercurial Tools Setup");
        setup.init();
        proxy.init();
    }

    //
    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        // LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
            // blocks
            event.getRegistry().register(new CondensingHopper());
            event.getRegistry().register(new EnderKeystone(0).setRegistryName(Names.ENDER_KEYSTONE));
            event.getRegistry().register(new EnderKeystone(1).setRegistryName(Names.ENDER_KEYSTONE_DAMPENED_I));
            event.getRegistry().register(new EnderKeystone(2).setRegistryName(Names.ENDER_KEYSTONE_DAMPENED_II));
            event.getRegistry().register(new EnderKeystone(3).setRegistryName(Names.ENDER_KEYSTONE_DAMPENED_III));
            event.getRegistry().register(new EnderVacuum());
            event.getRegistry().register(new EnticingPrism());
            event.getRegistry().register(new ExpandingHopper());
            event.getRegistry().register(new GrowthPulser());
            event.getRegistry().register(new Interloper());
            event.getRegistry().register(new MercurialGrindstone());
            event.getRegistry().register(new SpawnerTemplate());
        }

        @SubscribeEvent
        public static void onContainersRegistry(final RegistryEvent.Register<ContainerType<?>> event) {
            event.getRegistry().register(IForgeContainerType.create(
                    (windowId, inv, data) -> {
                        return new MercurialGrindstoneContainer(windowId, inv);
                    }).setRegistryName(Names.MERCURIAL_GRINDSTONE));
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            Item.Properties properties = new Item.Properties().group(setup.itemGroup);

            // item blocks
            event.getRegistry().register(new BlockItem(ModBlocks.CONDENSING_HOPPER, properties).setRegistryName(Names.CONDENSING_HOPPER));
            event.getRegistry().register(new BlockItem(ModBlocks.ENDER_KEYSTONE, properties).setRegistryName(Names.ENDER_KEYSTONE));
            event.getRegistry().register(new BlockItem(ModBlocks.ENDER_KEYSTONE_DAMPENED_I, properties).setRegistryName(Names.ENDER_KEYSTONE_DAMPENED_I));
            event.getRegistry().register(new BlockItem(ModBlocks.ENDER_KEYSTONE_DAMPENED_II, properties).setRegistryName(Names.ENDER_KEYSTONE_DAMPENED_II));
            event.getRegistry().register(new BlockItem(ModBlocks.ENDER_KEYSTONE_DAMPENED_III, properties).setRegistryName(Names.ENDER_KEYSTONE_DAMPENED_III));
            event.getRegistry().register(new BlockItem(ModBlocks.ENDER_VACUUM, properties).setRegistryName(Names.ENDER_VACUUM));
            event.getRegistry().register(new BlockItem(ModBlocks.ENTICING_PRISM, properties).setRegistryName(Names.ENTICING_PRISM));
            event.getRegistry().register(new BlockItem(ModBlocks.EXPANDING_HOPPER, properties).setRegistryName(Names.EXPANDING_HOPPER));
            event.getRegistry().register(new BlockItem(ModBlocks.GROWTH_PULSER, properties).setRegistryName(Names.GROWTH_PULSER));
            event.getRegistry().register(new BlockItem(ModBlocks.INTERLOPER, properties).setRegistryName(Names.INTERLOPER));
            event.getRegistry().register(new BlockItem(ModBlocks.MERCURIAL_GRINDSTONE, properties).setRegistryName(Names.MERCURIAL_GRINDSTONE));
            event.getRegistry().register(new BlockItem(ModBlocks.SPAWNER_TEMPLATE, properties).setRegistryName(Names.SPAWNER_TEMPLATE));

            // plain ol' items
            event.getRegistry().register(new CrystalCompass());
            event.getRegistry().register(new CubingTalisman());
            event.getRegistry().register(new MercurialBlend());
            event.getRegistry().register(new ModeratingGeode());
            event.getRegistry().register(new PotionBelt());
            event.getRegistry().register(new Quiver());
            event.getRegistry().register(new SoulTome());
        }

        @SubscribeEvent
        public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
            event.getRegistry().register(TileEntityType.Builder.create(CondensingHopperTile::new, ModBlocks.CONDENSING_HOPPER).build(null).setRegistryName(Names.CONDENSING_HOPPER));
            event.getRegistry().register(TileEntityType.Builder
                    .create(EnderKeystoneTile::new,
                            ModBlocks.ENDER_KEYSTONE,
                            ModBlocks.ENDER_KEYSTONE_DAMPENED_I,
                            ModBlocks.ENDER_KEYSTONE_DAMPENED_II,
                            ModBlocks.ENDER_KEYSTONE_DAMPENED_III)
                    .build(null)
                    .setRegistryName(Names.ENDER_KEYSTONE));
            event.getRegistry().register(TileEntityType.Builder.create(EnderVacuumTile::new, ModBlocks.ENDER_VACUUM).build(null).setRegistryName(Names.ENDER_VACUUM));
            event.getRegistry().register(TileEntityType.Builder.create(ExpandingHopperTile::new, ModBlocks.EXPANDING_HOPPER).build(null).setRegistryName(Names.EXPANDING_HOPPER));
            event.getRegistry().register(TileEntityType.Builder.create(InterloperTile::new, ModBlocks.INTERLOPER).build(null).setRegistryName(Names.INTERLOPER));
            event.getRegistry().register(TileEntityType.Builder.create(GrowthPulserTile::new, ModBlocks.GROWTH_PULSER).build(null).setRegistryName(Names.GROWTH_PULSER));
            event.getRegistry().register(TileEntityType.Builder.create(EnticingPrismTile::new, ModBlocks.ENTICING_PRISM).build(null).setRegistryName(Names.ENTICING_PRISM));
        }
    }
//
//    private void enqueueIMC(final InterModEnqueueEvent event)
//    {
//        // some example code to dispatch IMC to another mod
//        // InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
//    }
//
//    private void processIMC(final InterModProcessEvent event)
//    {
//        // some example code to receive and process InterModComms from other mods
//        /* LOGGER.info("Got IMC {}", event.getIMCStream().
//                map(m->m.getMessageSupplier().get()).
//                collect(Collectors.toList()));
//        */
//    }
//    // You can use SubscribeEvent and let the Event Bus discover methods to call
//    @SubscribeEvent
//    public void onServerStarting(FMLServerStartingEvent event) {
//        // do something when the server starts
//        // LOGGER.info("HELLO from server starting");
//    }
}
