package com.mercuriusxeno.mercurialtools.setup;


import com.mercuriusxeno.mercurialtools.blocks.ModBlocks;
import com.mercuriusxeno.mercurialtools.blocks.SpawnerTemplate;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class RegistryEvents {
    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
        // register a new block here
        // LOGGER.info("HELLO from Register Block");
        event.getRegistry().register(new SpawnerTemplate());
    }

    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
        // register a new block here
        // LOGGER.info("HELLO from Register Block");
        event.getRegistry().register(new BlockItem(ModBlocks.SPAWNER_TEMPLATE, new Item.Properties()).setRegistryName(Names.SPAWNER_TEMPLATE));
    }
}
