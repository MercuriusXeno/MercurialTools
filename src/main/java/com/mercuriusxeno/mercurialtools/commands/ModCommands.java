package com.mercuriusxeno.mercurialtools.commands;

import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {
    public static void register (CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdMerc = dispatcher.register(
                Commands.literal(Names.MOD_ID)
                // register commands
                //.then(CommandTest.register(dispatcher));
        );
        dispatcher.register(Commands.literal("merc").redirect(cmdMerc));
    }
}
