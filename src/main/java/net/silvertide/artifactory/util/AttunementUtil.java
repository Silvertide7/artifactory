package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.component.AttunementDataSource;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.registry.AttributeRegistry;

import java.util.*;

/*
 * This class is used to get information about an attuned item or a player
 */
public final class AttunementUtil {
    private AttunementUtil() {}

    public static int getMaxAttunementSlots(Player player) {
        return (int) player.getAttributeValue(AttributeRegistry.ATTUNEMENT_SLOTS);
    }

    public static int getAttunementSlotsUsed(Player player) {
        Map<UUID, AttunedItem> attunedItems = ArtifactorySavedData.get().getAttunedItems(player.getUUID());
        int numAttunementSlotsUsed = 0;
        for(AttunedItem attunedItem : attunedItems.values()) {
            numAttunementSlotsUsed += DataPackUtil.getAttunementData(attunedItem.getResourceLocation()).map(AttunementDataSource::attunementSlotsUsed).orElse(0);
        }
        return numAttunementSlotsUsed;
    }

    public static int getOpenAttunementSlots(Player player) {
        return getMaxAttunementSlots(player) - getAttunementSlotsUsed(player);
    }

    public static int getLevelOfAttunementAchieved(ItemStack stack) {
        return DataComponentUtil.getAttunementData(stack).map(attunementData -> {
            if(attunementData.attunementUUID() != null && attunementData.attunedToUUID() != null) {
                return getLevelOfAttunementAchieved(attunementData.attunedToUUID(), attunementData.attunementUUID());
            }
            return 0;
        }).orElse(0);
    }

    public static int getLevelOfAttunementAchievedByPlayer(ServerPlayer player, ItemStack stack) {
        return DataComponentUtil.getAttunementData(stack).map(attunementData -> {
            if(attunementData.attunementUUID() != null
                    && attunementData.attunedToUUID() != null
                    && player.getUUID().equals(attunementData.attunedToUUID())) {
                return getLevelOfAttunementAchieved(attunementData.attunedToUUID(), attunementData.attunementUUID());
            }
            return 0;
        }).orElse(0);
    }

    public static int getLevelOfAttunementAchieved(UUID playerUUID, UUID itemAttunementUUID) {
        return ArtifactorySavedData.get().getAttunedItem(playerUUID, itemAttunementUUID).map(AttunedItem::getAttunementLevel).orElse(0);
    }

    public static boolean doesPlayerHaveSlotCapacityToAttuneItem(Player player, AttunementDataSource attunementData) {
        int openSlots = getOpenAttunementSlots(player);
        int attunementSlotsRequired = attunementData.getAttunementSlotsUsed();
        boolean uniqueRestrictionIsActive = attunementData.unique() && AttunementUtil.isPlayerAtUniqueAttunementLimit(player.getUUID());
        return openSlots >= attunementSlotsRequired && !uniqueRestrictionIsActive;
    }

    public static boolean canIncreaseAttunementLevel(Player player, ItemStack stack) {
        if(stack.isEmpty() || !AttunementUtil.isValidAttunementItem(stack)) return false;
        return DataPackUtil.getAttunementData(stack).map(attunementData -> {
            if(isItemAttunedToPlayer(player, stack)) {
                int levelAchieved = getLevelOfAttunementAchieved(stack);
                int maxLevel = DataPackUtil.getNumAttunementLevels(stack);
                return levelAchieved < maxLevel;
            } else {
                return isAvailableToAttune(stack) && doesPlayerHaveSlotCapacityToAttuneItem(player, attunementData);
            }
        }).orElse(false);
    }

    public static boolean isItemAttunedToAPlayer(ItemStack stack) {
        if(stack.isEmpty()) return false;
        return DataComponentUtil.getAttunementData(stack).map(attunementData -> attunementData.attunedToUUID() != null && attunementData.attunementUUID() != null).orElse(false);
    }

    public static boolean isUseRestricted(Player player, ItemStack stack) {
        if(!isValidAttunementItem(stack)) return false;
        return DataPackUtil.getAttunementData(stack).map(itemAttunementData -> {
            if(isAttunedToAnotherPlayer(player, stack)) {
                if(!player.level().isClientSide()) {
                    PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.owned_by_another_player");
                }
                return true;
            } else if(!AttunementUtil.isItemAttunedToPlayer(player, stack) && !itemAttunementData.useWithoutAttunement()) {
                if(!player.level().isClientSide()) {
                    PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.item_not_usable");
                }
                return true;
            }
            return false;
        }).orElse(false);
    }

    // Soulbound is active if the soulbound flag is set and the player uuid matches the stack attuned to uuid.
    public static boolean isSoulboundActive(ServerPlayer player, ItemStack stack) {
        return AttunementUtil.isValidAttunementItem(stack) && DataComponentUtil.getAttunementData(stack).map(attunementData -> attunementData.isSoulbound() && attunementData.attunedToUUID().equals(player.getUUID())).orElse(false);
    }

    public static boolean isAttunedToAnotherPlayer(Player player, ItemStack stack) {
        if(stack.isEmpty()) return false;
        return DataComponentUtil.getAttunementData(stack).map(attunementData -> {
            if(attunementData.attunedToUUID() != null) {
                return !player.getUUID().equals(attunementData.attunedToUUID());
            }
            return false;
        }).orElse(false);
    }

    public static boolean arePlayerAndItemAttuned(Player player, ItemStack stack) {
        return isItemAttunedToPlayer(player, stack) && isPlayerAttunedToItem(player, stack);
    }

    public static boolean isItemAttunedToPlayer(Player player, ItemStack stack) {
        if(stack.isEmpty()) return false;
        return DataComponentUtil.getAttunementData(stack).map(attunementData -> {
            if(attunementData.attunedToUUID() == null) return false;
            return attunementData.attunedToUUID().equals(player.getUUID());
        }).orElse(false);
    }

    private static boolean isPlayerAttunedToItem(Player player, ItemStack stack) {
        return DataComponentUtil.getAttunementData(stack).map(attunementData ->
            ArtifactorySavedData.get().getAttunedItem(player.getUUID(), attunementData.attunementUUID()).isPresent())
                .orElse(false);
    }

    public static boolean doesPlayerHaveAttunedItem(Player player) {
        return !ArtifactorySavedData.get().getAttunedItems(player.getUUID()).isEmpty();
    }

    public static boolean isAvailableToAttune(ItemStack stack) {
        boolean uniqueRestrictionActive = false;
        if(DataPackUtil.isUniqueAttunement(stack)) {
            uniqueRestrictionActive = !AttunementUtil.getPlayerUUIDsWithAttunementToItem(ResourceLocationUtil.getResourceLocation(stack)).isEmpty();
        }
        boolean alreadyAttuned = DataComponentUtil.getAttunementData(stack).map(attunementData -> attunementData.attunedToUUID() != null).orElse(false);
        return isValidAttunementItem(stack) && !alreadyAttuned && !uniqueRestrictionActive;
    }

    public static boolean isValidAttunementItem(ItemStack stack) {
        return !stack.isEmpty() && DataPackUtil.getAttunementData(stack).map(attunementData -> attunementData.attunementSlotsUsed() >= 0).orElse(false);
    }

    public static String getAttunedItemDisplayName(ItemStack stack) {
        return GUIUtil.prettifyName(DataComponentUtil.getItemDisplayName(stack).orElse(stack.getItem().toString()));
    }

    public static Optional<String> getSavedDataAttunedItemOwnerDisplayName(ItemStack stack) {
        return DataComponentUtil.getAttunementData(stack).flatMap(attunementData -> ArtifactorySavedData.get().getPlayerName(attunementData.attunedToUUID()));
    }

    public static boolean isPlayerAtUniqueAttunementLimit(UUID playerUUID) {
        return getPlayersNumberOfUniqueAttunements(playerUUID) >= ServerConfigs.NUMBER_UNIQUE_ATTUNEMENTS_PER_PLAYER.get();
    }

    public static int getPlayersNumberOfUniqueAttunements(UUID playerUUID) {
        Collection<AttunedItem> attunedItems = ArtifactorySavedData.get().getAttunedItems(playerUUID).values();
        if(attunedItems.isEmpty()) return 0;

        int numUniques = 0;
        for(AttunedItem item : attunedItems) {
            if(DataPackUtil.isUniqueAttunement(item.getResourceLocation())) {
                numUniques++;
            }
        }
        return numUniques;
    }

    public static List<UUID> getPlayerUUIDsWithAttunementToItem(ResourceLocation resourceLocation) {
        List<UUID> results = new ArrayList<>();
        Map<UUID, Map<UUID, AttunedItem>> allAttunedItems = ArtifactorySavedData.get().getAttunedItemsMap();

        for(Map.Entry<UUID, Map<UUID, AttunedItem>> entry : allAttunedItems.entrySet()) {
            for(AttunedItem attunedItem : entry.getValue().values()) {
                if(resourceLocation.toString().equals(attunedItem.getResourceLocation())) {
                    results.add(entry.getKey());
                }
            }
        }
        return results;
    }
}
