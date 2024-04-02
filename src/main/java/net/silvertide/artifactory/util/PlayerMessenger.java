package net.silvertide.artifactory.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class PlayerMessenger {
    private PlayerMessenger() {}

    public static void displayClientMessage(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }
    public static void displayTranslatabelClientMessage(Player player, String translationKey) {
        player.displayClientMessage(Component.translatable(translationKey), true);
    }
    public static void sendSystemMessage(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    public static void sendTranslatableSystemMessage(Player player, String translationKey) {
        player.sendSystemMessage(Component.translatable(translationKey));
    }
}
