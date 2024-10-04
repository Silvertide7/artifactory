package net.silvertide.artifactory.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;

public final class AttunementService {
    private AttunementService() {}

    // Atunement Actions
    public static void increaseLevelOfAttunement(Player player, ItemStack stack) {
        int levelOfAttunementAchieved = AttunementUtil.getLevelOfAttunementAchieved(stack);
        boolean successfulAttunementIncrease = false;

        if(levelOfAttunementAchieved == 0 && !AttunementUtil.isItemAttunedToAPlayer(stack)) {
            successfulAttunementIncrease = attuneItemAndPlayer(player, stack);
        } else if(levelOfAttunementAchieved > 0 && AttunementUtil.isItemAttunedToPlayer(player, stack)) {
            successfulAttunementIncrease = StackNBTUtil.getItemAttunementUUID(stack).map(attunedToUUID -> ArtifactorySavedData.get().increaseLevelOfAttunedItem(player.getUUID(), attunedToUUID)).orElse(false);
        }
        if (successfulAttunementIncrease) {
            ModificationUtil.updateItemWithAttunementModifications(stack, levelOfAttunementAchieved + 1);
        }
    }

    // Returns true if the item was successfully attuned, and false if not.
    private static boolean attuneItemAndPlayer(Player player, ItemStack stack) {
        if (AttunementUtil.canIncreaseAttunementLevel(player, stack)) {
            StackNBTUtil.setupStackToAttune(stack);
            linkPlayerAndItem(player, stack);
            return true;
        }
        return false;
    }

    private static void linkPlayerAndItem(Player player, ItemStack stack) {
        AttunedItem.buildAttunedItem(player, stack).ifPresent(attunedItem -> {
            ArtifactorySavedData.get().setAttunedItem(player.getUUID(), attunedItem);
            StackNBTUtil.putPlayerDataInArtifactoryTag(player, stack);
        });
    }

    public static void removeAttunementFromPlayerAndItem(ItemStack stack) {
        StackNBTUtil.getAttunedToUUID(stack).ifPresent(attunedToUUID -> {
            StackNBTUtil.getItemAttunementUUID(stack).ifPresent(itemAttunementUUID -> {
                ArtifactorySavedData artifactorySavedData = ArtifactorySavedData.get();

                if (ModificationUtil.hasModification(stack, "unbreakable")) {
                    StackNBTUtil.removeUnbreakable(stack);
                }

                artifactorySavedData.removeAttunedItem(attunedToUUID, itemAttunementUUID);
            });
        });

        // Clear the attunement data off of the item itself.
        StackNBTUtil.removeArtifactoryTag(stack);
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
                if (ModificationUtil.hasModification(stack, "unbreakable")) {
                    StackNBTUtil.removeUnbreakable(stack);
                }
                StackNBTUtil.removeArtifactoryTag(stack);
                return true;
            }
            return false;
        })).orElse(false);

    }

    public static void applyEffectsToPlayer(Player player, ItemStack stack) {
        if(AttunementUtil.isValidAttunementItem(stack)) {
            if(AttunementUtil.isAttunedToAnotherPlayer(player, stack)) {
                EffectUtil.applyMobEffectInstancesToPlayer(player, Config.EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM.get());
            } else if (!AttunementUtil.isItemAttunedToPlayer(player, stack) && !AttunementUtil.canUseWithoutAttunement(stack)) {
                EffectUtil.applyMobEffectInstancesToPlayer(player, Config.WEAR_EFFECTS_WHEN_USE_RESTRICTED.get());
            }
        }
    }
}
