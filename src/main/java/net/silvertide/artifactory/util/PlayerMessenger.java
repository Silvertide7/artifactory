package net.silvertide.artifactory.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerMessenger {
    private PlayerMessenger() {}

    public static void displayClientMessage(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    public static void displayTranslatableClientMessage(ServerPlayer player, String key) {
        // FakePlayer or disconnect safety
        if (player.server.getPlayerList().getPlayer(player.getUUID()) == null) return;

        player.displayClientMessage(Component.translatable(key), true);
    }

    public static void displayTranslatableClientMessage(ServerPlayer player, String translationKey, String extraValue) {
        if (player.server.getPlayerList().getPlayer(player.getUUID()) == null) return;
        player.displayClientMessage(Component.translatable(translationKey, extraValue), true);
    }
    public static void sendSystemMessage(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    public static void sendTranslatableSystemMessage(Player player, String translationKey) {
        player.sendSystemMessage(Component.translatable(translationKey));
    }
}
