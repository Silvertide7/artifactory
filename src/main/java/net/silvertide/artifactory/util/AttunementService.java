package net.silvertide.artifactory.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;

public final class AttunementService {
    private AttunementService() {}

    // Atunement Actions
    public static void increaseLevelOfAttunement(ServerPlayer player, ItemStack stack) {
        int levelOfAttunementAchieved = AttunementUtil.getLevelOfAttunementAchieved(stack);
        boolean successfulAttunementIncrease = false;

        if(levelOfAttunementAchieved == 0 && !AttunementUtil.isItemAttunedToAPlayer(stack)) {
            successfulAttunementIncrease = attuneItemAndPlayer(player, stack);
        } else if(levelOfAttunementAchieved > 0 && AttunementUtil.isItemAttunedToPlayer(player, stack)) {
            successfulAttunementIncrease = StackNBTUtil.getItemAttunementUUID(stack).map(attunedToUUID -> ArtifactorySavedData.get().increaseLevelOfAttunedItem(player.getUUID(), attunedToUUID)).orElse(false);
        }
        if (successfulAttunementIncrease) {
            ModificationService.applyAttunementModifications(stack);
        }
    }

    // Returns true if the item was successfully attuned, and false if not.
    private static boolean attuneItemAndPlayer(ServerPlayer player, ItemStack stack) {
        if (AttunementUtil.canIncreaseAttunementLevel(player, stack)) {
            StackNBTUtil.setupStackToAttune(stack);
            linkPlayerAndItem(player, stack);
            return true;
        }
        return false;
    }

    private static void linkPlayerAndItem(ServerPlayer player, ItemStack stack) {
        AttunedItem.buildAttunedItem(player, stack).ifPresent(attunedItem -> {
            ArtifactorySavedData.get().setAttunedItem(player, attunedItem);
            StackNBTUtil.putPlayerDataInArtifactoryTag(player, stack);
        });
    }

    public static void removeAttunementFromPlayerAndItem(ItemStack stack) {
        StackNBTUtil.getAttunedToUUID(stack).ifPresent(attunedToUUID -> {
            StackNBTUtil.getItemAttunementUUID(stack).ifPresent(itemAttunementUUID -> {
                if (StackNBTUtil.isUnbreakableFromArtifactory(stack)) {
                    StackNBTUtil.removeUnbreakable(stack);
                }
                ArtifactorySavedData.get().removeAttunedItem(attunedToUUID, itemAttunementUUID);
            });
        });

        // Clear the attunement data off of the item itself.
        StackNBTUtil.removeArtifactoryNBT(stack);
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

        return StackNBTUtil.getAttunedToUUID(stack).flatMap(playerUUID -> StackNBTUtil.getItemAttunementUUID(stack).map(itemAttunementUUID -> {
            if(ArtifactorySavedData.get().getAttunedItem(playerUUID, itemAttunementUUID).isEmpty()) {
                if (StackNBTUtil.isUnbreakableFromArtifactory(stack)) {
                    StackNBTUtil.removeUnbreakable(stack);
                }
                StackNBTUtil.removeArtifactoryNBT(stack);
                return true;
            }
            return false;
        })).orElse(false);

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
