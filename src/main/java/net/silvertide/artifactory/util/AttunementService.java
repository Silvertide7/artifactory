package net.silvertide.artifactory.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.Optional;
import java.util.UUID;

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
        Optional<UUID> attunedToUUID = StackNBTUtil.getAttunedToUUID(stack);
        Optional<UUID> itemAttunementUUID = StackNBTUtil.getItemAttunementUUID(stack);

        // Check if player attunement data contains the item and remove it if so.
        if(itemAttunementUUID.isPresent() && attunedToUUID.isPresent()) {
            ArtifactorySavedData artifactorySavedData = ArtifactorySavedData.get();

            if (ModificationUtil.hasModification(stack, "unbreakable")) {
                StackNBTUtil.removeUnbreakable(stack);
            }

            artifactorySavedData.removeAttunedItem(attunedToUUID.get(), itemAttunementUUID.get());
        }

        // Clear the attunement data off of the item itself.
        StackNBTUtil.removeArtifactoryTag(stack);
    }

    // The purpose of this method is to check if the itemstack is attuned to a player
    // but that player is no longer attuned to that item. If so clear the items attunement
    // data, so it can be attuned again.
    public static void clearAttunementIfBrokenByPlayer(ItemStack stack) {
        StackNBTUtil.getAttunedToUUID(stack).ifPresent(playerUUID -> {
            StackNBTUtil.getItemAttunementUUID(stack).ifPresent(itemAttunementUUID -> {
                if(ArtifactorySavedData.get().getAttunedItem(playerUUID, itemAttunementUUID).isEmpty()) {
                    if (ModificationUtil.hasModification(stack, "unbreakable")) {
                        StackNBTUtil.removeUnbreakable(stack);
                    }
                    StackNBTUtil.removeArtifactoryTag(stack);
                }
            });
        });
    }
}
