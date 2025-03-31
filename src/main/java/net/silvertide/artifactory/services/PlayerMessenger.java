package net.silvertide.artifactory.services;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class PlayerMessenger {
    private PlayerMessenger() {}

    public static void displayTranslatabelClientMessage(Player player, String translationKey) {
        player.displayClientMessage(Component.translatable(translationKey), true);
    }

    public static void displayTranslatabelClientMessage(Player player, String translationKey, String extraValue) {
        player.displayClientMessage(Component.translatable(translationKey, extraValue), true);
    }
    public static void sendSystemMessage(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
