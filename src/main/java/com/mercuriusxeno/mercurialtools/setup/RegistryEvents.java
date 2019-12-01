package com.mercuriusxeno.mercurialtools.setup;


import com.mercuriusxeno.mercurialtools.block.*;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.codehaus.plexus.util.Expand;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class RegistryEvents {
    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new CondensingHopper());
        event.getRegistry().register(new EnderKeystone());
        event.getRegistry().register(new EnderVacuum());
        event.getRegistry().register(new EnticingPrism());
        event.getRegistry().register(new ExpandingHopper());
        event.getRegistry().register(new GrowthPulser());
        event.getRegistry().register(new Interloper());
        event.getRegistry().register(new SpawnerTemplate());
    }

    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new BlockItem(ModBlocks.CONDENSING_HOPPER, new Item.Properties()).setRegistryName(Names.CONDENSING_HOPPER));
        event.getRegistry().register(new BlockItem(ModBlocks.ENDER_KEYSTONE, new Item.Properties()).setRegistryName(Names.ENDER_KEYSTONE));
        event.getRegistry().register(new BlockItem(ModBlocks.ENDER_VACUUM, new Item.Properties()).setRegistryName(Names.ENDER_VACUUM));
        event.getRegistry().register(new BlockItem(ModBlocks.ENTICING_PRISM, new Item.Properties()).setRegistryName(Names.ENTICING_PRISM));
        event.getRegistry().register(new BlockItem(ModBlocks.EXPANDING_HOPPER, new Item.Properties()).setRegistryName(Names.EXPANDING_HOPPER));
        event.getRegistry().register(new BlockItem(ModBlocks.GROWTH_PULSER, new Item.Properties()).setRegistryName(Names.GROWTH_PULSER));
        event.getRegistry().register(new BlockItem(ModBlocks.INTERLOPER, new Item.Properties()).setRegistryName(Names.INTERLOPER));
        event.getRegistry().register(new BlockItem(ModBlocks.SPAWNER_TEMPLATE, new Item.Properties()).setRegistryName(Names.SPAWNER_TEMPLATE));
    }
}
