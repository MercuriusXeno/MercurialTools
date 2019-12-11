package com.mercuriusxeno.mercurialtools.network;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
    private static final String PROTOCOL_VERSION = "1";
    private static int ID = 0;
    public static int nextId() { return ID++; }
    public static SimpleChannel INSTANCE;

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Names.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        // handle registering packets and things.
        // INSTANCE.registerMessage(nextId(), SoulTomeMessage.class, SoulTomeMessage::toBytes, SoulTomeMessage::new, SoulTomeMessage::handle);
    }
}
