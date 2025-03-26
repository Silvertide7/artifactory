package net.silvertide.artifactory.services;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.*;

public final class AttunementService {
    private AttunementService() {}

    // Attunement Actions
    // This is the main entry point to increase an attunement level.
    public static void increaseLevelOfAttunement(ServerPlayer player, ItemStack stack) {
        int levelOfAttunementAchieved = AttunementUtil.getLevelOfAttunementAchieved(stack);
        boolean successfulAttunementIncrease = false;

        if(levelOfAttunementAchieved == 0 && !AttunementUtil.isItemAttunedToAPlayer(stack)) {
            successfulAttunementIncrease = attuneItemAndPlayer(player, stack);
        } else if(levelOfAttunementAchieved > 0 && AttunementUtil.isItemAttunedToPlayer(player, stack)) {
            successfulAttunementIncrease = DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> {
                if(attunementData.attunementUUID() != null) {
                    return ArtifactorySavedData.get().increaseLevelOfAttunedItem(player.getUUID(), attunementData.attunementUUID());
                }
                return false;
            }).orElse(false);
        }
        if (successfulAttunementIncrease) {
            ModificationService.applyAttunementModifications(stack);
        }
    }

    // Returns true if the item was successfully attuned, and false if not.
    private static boolean attuneItemAndPlayer(ServerPlayer player, ItemStack stack) {
        if (AttunementUtil.canIncreaseAttunementLevel(player, stack)) {
            AttunedItem attunedItem = AttunedItem.buildAttunedItem(player, stack);
            DataComponentUtil.configurePlayerAttunementData(player, stack, attunedItem);
            ArtifactorySavedData.get().setAttunedItem(player, attunedItem);
            return true;
        }
        return false;
    }

    public static void removeAttunementFromPlayerAndItem(ItemStack stack) {
        DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
            removeUnbreakableIfFromArtifactory(stack, attunementData);
            ArtifactorySavedData.get().removeAttunedItem(attunementData.attunedToUUID(), attunementData.attunementUUID());
        });

        // Clear the attunement data off of the item itself.
        DataComponentUtil.clearPlayerAttunementData(stack);
    }

    public static void clearBrokenAttunements(Player player) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            clearBrokenAttunementIfExists(player.getInventory().items.get(i));
        }
    }

    // The purpose of this method is to check if the itemstack is attuned to a player
    // but that player is no longer attuned to that item. If so clear the items attunement
    // data, so it can be attuned again. Returns true if a broken attunement was cleaned up.
    public static void clearBrokenAttunementIfExists(ItemStack stack) {
        if(stack.isEmpty()) return;

        DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
            if (attunementData.attunedToUUID() != null
                    && ArtifactorySavedData.get().getAttunedItem(attunementData.attunedToUUID(), attunementData.attunementUUID()).isEmpty()) {
                removeUnbreakableIfFromArtifactory(stack, attunementData);
                DataComponentUtil.clearPlayerAttunementData(stack);
            }
        });
    }

    public static void removeUnbreakableIfFromArtifactory(ItemStack stack, PlayerAttunementData playerAttunementData) {
        if (playerAttunementData.isUnbreakable() && stack.get(DataComponents.UNBREAKABLE) != null) {
            DataComponentUtil.removeUnbreakable(stack);
        }
    }

    public static void applyEffectsToPlayer(Player player, ItemStack stack, boolean wearable) {
        if(AttunementUtil.isValidAttunementItem(stack)) {
            if(AttunementUtil.isAttunedToAnotherPlayer(player, stack)) {
                EffectUtil.applyMobEffectInstancesToPlayer(player, ServerConfigs.EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM.get());
            } else if (wearable && !AttunementUtil.isItemAttunedToPlayer(player, stack) && !AttunementDataSourceUtil.canUseWithoutAttunement(stack)) {
                EffectUtil.applyMobEffectInstancesToPlayer(player, ServerConfigs.WEAR_EFFECTS_WHEN_USE_RESTRICTED.get());
            }
        }
    }
}
