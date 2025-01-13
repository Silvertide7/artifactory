package net.silvertide.artifactory.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.registry.DataComponentRegistry;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;

public final class AttunementService {
    private AttunementService() {}

    // Atunement Actions
    // This is the main entry point to increase an attunement level.


    // Returns true if the item was successfully attuned, and false if not.
    private static boolean attuneItemAndPlayer(ServerPlayer player, ItemStack stack) {
        if (AttunementUtil.canIncreaseAttunementLevel(player, stack)) {
            DataComponentUtil.setupAttunementData(stack);

            AttunedItem.buildAttunedItem(player, stack).ifPresent(attunedItem -> {
                ArtifactorySavedData.get().setAttunedItem(player, attunedItem);
                DataComponentUtil.configureAttunementData(player, stack);
            });
            return true;
        }
        return false;
    }

    public static void removeAttunementFromPlayerAndItem(ItemStack stack) {
        DataComponentUtil.getAttunementData(stack).ifPresent(attunementData -> {
            if(attunementData.isUnbreakable() && DataComponentUtil.isUnbreakable(stack)) {
                DataComponentUtil.removeUnbreakable(stack);
            }
            ArtifactorySavedData.get().removeAttunedItem(attunementData.attunedToUUID(), attunementData.attunementUUID());
        });

        // Clear the attunement data off of the item itself.
        DataComponentUtil.clearAttunementData(stack);
    }

    public static void clearBrokenAttunements(Player player) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            clearBrokenAttunementIfExists(player.getInventory().items.get(i));
        }
    }

    // The purpose of this method is to check if the itemstack is attuned to a player
    // but that player is no longer attuned to that item. If so clear the items attunement
    // data, so it can be attuned again. Returns true if a broken attunement was cleaned up.
    public static boolean clearBrokenAttunementIfExists(ItemStack stack) {
        if(stack.isEmpty()) return false;

        return DataComponentUtil.getAttunementData(stack).map(attunementData -> {
            if(attunementData.attunedToUUID() != null
                    && ArtifactorySavedData.get().getAttunedItem(attunementData.attunedToUUID(), attunementData.attunementUUID()).isEmpty()) {
                if(attunementData.isUnbreakable() && stack.get(DataComponents.UNBREAKABLE) != null) {
                    stack.set(DataComponents.UNBREAKABLE, null);
                }
                stack.set(DataComponentRegistry.ATTUNEMENT_DATA, null);
                return true;
            }
            return false;
        }).orElse(false);
    }

    public static void applyEffectsToPlayer(Player player, ItemStack stack, boolean wearable) {
        if(AttunementUtil.isValidAttunementItem(stack)) {
            if(AttunementUtil.isAttunedToAnotherPlayer(player, stack)) {
                EffectUtil.applyMobEffectInstancesToPlayer(player, ServerConfigs.EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM.get());
            } else if (wearable && !AttunementUtil.isItemAttunedToPlayer(player, stack) && !DataPackUtil.canUseWithoutAttunement(stack)) {
                EffectUtil.applyMobEffectInstancesToPlayer(player, ServerConfigs.WEAR_EFFECTS_WHEN_USE_RESTRICTED.get());
            }
        }
    }
}
