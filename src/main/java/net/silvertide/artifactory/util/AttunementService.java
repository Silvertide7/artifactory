package net.silvertide.artifactory.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.Optional;
import java.util.UUID;

public final class AttunementService {
    private AttunementService() {}

    // Atunement Actions

    public static void increaseLevelOfAttunement(Player player, ItemStack stack) {
        DataPackUtil.getAttunementData(stack).ifPresent(attunementData -> {
            int levelOfAttunementAchieved = AttunementUtil.getLevelOfAttunementAchieved(stack);
            if(levelOfAttunementAchieved == 0 && !AttunementUtil.isItemAttunedToAPlayer(stack)) {
                attuneItemAndPlayer(player, stack, attunementData);
            } else if(levelOfAttunementAchieved > 0 && AttunementUtil.isItemAttunedToPlayer(player, stack)) {
                StackNBTUtil.getItemAttunementUUID(stack).ifPresent(attunedToUUID -> {
                    int newLevel = ArtifactorySavedData.get().increaseLevelOfAttunedItem(player.getUUID(), attunedToUUID);
                    if(newLevel > 1) {
                        ModificationUtil.updateItemWithAttunementModifications(stack, attunementData, newLevel);
                    }
                });
            }
        });
    }
    private static void attuneItemAndPlayer(Player player, ItemStack stack, ItemAttunementData attunementData) {
        if (AttunementUtil.canIncreaseAttunementLevel(player, stack, attunementData)) {
            StackNBTUtil.setupStackToAttune(stack);
            linkPlayerAndItem(player, stack);
            ModificationUtil.updateItemWithAttunementModifications(stack, attunementData, 1);
        }
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
}
