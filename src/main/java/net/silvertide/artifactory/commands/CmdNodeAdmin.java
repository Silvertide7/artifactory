package net.silvertide.artifactory.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.silvertide.artifactory.storage.ArtifactorySavedData;

public class CmdNodeAdmin {
    private static final String TARGET_ARG = "Target";

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("admin")
                .requires(p -> p.hasPermission(2))
                .then(Commands.argument(TARGET_ARG, EntityArgument.players())
                        .then(Commands.literal("attunementLevels")
                            .then(Commands.literal("breakAll")
                                .executes(CmdNodeAdmin::adminBreakAttunements)
                            )
                        )
                );
    }

    public static int adminBreakAttunements(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        for (ServerPlayer player : EntityArgument.getPlayers(ctx, TARGET_ARG)) {
            ArtifactorySavedData.get().removeAllAttunedItems(player.getUUID());
        }
        return 0;
    }
}
