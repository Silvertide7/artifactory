package net.silvertide.artifactory.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CmdRoot {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("artifactory")
                .then(CmdNodeAdmin.register(dispatcher))
                .then(CmdNodeGeneral.register(dispatcher)));
    }
}
