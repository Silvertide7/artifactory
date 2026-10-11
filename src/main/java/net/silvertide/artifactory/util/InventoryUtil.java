package net.silvertide.artifactory.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class InventoryUtil {
    private InventoryUtil() {}

    public static void giveOrDropAtFeet(ServerPlayer player, ItemStack stack) {
        Inventory inventory = player.getInventory();
        if (inventory.getFreeSlot() >= 0) inventory.add(stack);
        if (!stack.isEmpty()) player.drop(stack, false, false);
    }
}
