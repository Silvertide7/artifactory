package net.silvertide.artifactory.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AscensionUtil {

    private AscensionUtil() {}

    public static boolean canItemAscend(ItemStack stack, Player player) {
        if(AttunementUtil.arePlayerAndItemAttuned(player, stack)) {
            return AttunementUtil.getLevelOfAttunementAchieved(stack) < DataPackUtil.getMaxLevelOfAttunementPossible(stack);
        }
        return false;
    }

    public static void ascendAttunement(ItemStack stack, Player player) {
        DataPackUtil.getAttunementData(stack).ifPresent(attunementData -> {
            if (canItemAscend(stack, player)) {
                int nextLevel = AttunementUtil.getLevelOfAttunementAchieved(stack) + 1;
                ModificationUtil.updateItemWithAttunementModifications(stack, attunementData, nextLevel);
            }
        });
    }
}
