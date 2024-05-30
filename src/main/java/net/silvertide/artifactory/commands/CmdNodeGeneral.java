package net.silvertide.artifactory.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.PlayerMessenger;

public class CmdNodeGeneral {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("slots").executes(CmdNodeGeneral::getPlayerSlots);

    }
    public static int getPlayerSlots(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayer();
        if(player == null) return 0;

        String slotsUsedText = AttunementUtil.getAttunementSlotsUsed(player) + " of " + AttunementUtil.getMaxAttunementSlots(player) + " attunement slots used.";

        PlayerMessenger.sendSystemMessage(player, "§3" + slotsUsedText + "§r");
        return 0;
    }
}
