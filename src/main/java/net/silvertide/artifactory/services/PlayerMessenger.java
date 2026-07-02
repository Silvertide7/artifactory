package net.silvertide.artifactory.services;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
    public static void sendSystemMessage(ServerPlayer player, Component message) {
        player.sendSystemMessage(message);
    }
}
