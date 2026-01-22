package net.silvertide.artifactory.services;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerMessenger {
    private PlayerMessenger() {}

    public static void displayTranslatableClientMessage(ServerPlayer player, String translationKey) {
        if (player.server.getPlayerList().getPlayer(player.getUUID()) == null) return;
        player.displayClientMessage(Component.translatable(translationKey), true);
    }

    public static void displayTranslatableClientMessage(ServerPlayer player, String translationKey, String extraValue) {
        if (player.server.getPlayerList().getPlayer(player.getUUID()) == null) return;
        player.displayClientMessage(Component.translatable(translationKey, extraValue), true);
    }
    public static void sendSystemMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
