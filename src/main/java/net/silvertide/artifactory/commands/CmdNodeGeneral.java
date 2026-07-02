package net.silvertide.artifactory.commands;


import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.services.PlayerMessenger;

public class CmdNodeGeneral {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("slots").executes(CmdNodeGeneral::getPlayerSlots);

    }
    public static int getPlayerSlots(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if(player == null) return 0;

        Component slotsUsedText = Component.translatable("command.artifactory.slots_used",
                AttunementUtil.getAttunementSlotsUsed(player), AttunementUtil.getMaxAttunementSlots(player))
                .withStyle(ChatFormatting.DARK_AQUA);

        PlayerMessenger.sendSystemMessage(player, slotsUsedText);
        return 0;
    }
}
