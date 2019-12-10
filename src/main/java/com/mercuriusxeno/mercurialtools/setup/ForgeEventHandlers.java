package com.mercuriusxeno.mercurialtools.setup;

import com.mercuriusxeno.mercurialtools.commands.ModCommands;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class ForgeEventHandlers {
    @SubscribeEvent
    public void serverLoad(FMLServerStartingEvent event) {
        ModCommands.register(event.getCommandDispatcher());
    }
}
