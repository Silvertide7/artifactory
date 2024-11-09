package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.registry.AttributeRegistry;

import java.util.*;

/*
 * This class is used to get information about an attuned item or a player
 */
public final class AttunementUtil {
    private AttunementUtil() {}

    public static int getMaxAttunementSlots(Player player) {
        return (int) player.getAttributeValue(AttributeRegistry.MAX_ATTUNEMENT_SLOTS.get());
    }

    public static int getAttunementSlotsUsed(Player player) {
        Map<UUID, AttunedItem> attunedItems = ArtifactorySavedData.get().getAttunedItems(player.getUUID());
        int numAttunementSlotsUsed = 0;
        for(AttunedItem attunedItem : attunedItems.values()) {
            numAttunementSlotsUsed += DataPackUtil.getAttunementData(attunedItem.getResourceLocation()).map(ItemAttunementData::attunementSlotsUsed).orElse(0);
        }
        return numAttunementSlotsUsed;
    }

    public static int getOpenAttunementSlots(Player player) {
        return getMaxAttunementSlots(player) - getAttunementSlotsUsed(player);
    }

    public static int getLevelOfAttunementAchieved(ItemStack stack) {
        Optional<UUID> attunedToUUID = StackNBTUtil.getAttunedToUUID(stack);
        Optional<UUID> itemAttunementUUID = StackNBTUtil.getItemAttunementUUID(stack);

        if(itemAttunementUUID.isPresent() && attunedToUUID.isPresent()) {
            return getLevelOfAttunementAchieved(attunedToUUID.get(), itemAttunementUUID.get());
        }
        return 0;
    }

    public static int getLevelOfAttunementAchievedByPlayer(ServerPlayer player, ItemStack stack) {
        Optional<UUID> itemAttunementUUID = StackNBTUtil.getItemAttunementUUID(stack);
        Optional<UUID> attunedToUUID = StackNBTUtil.getAttunedToUUID(stack);

        if(itemAttunementUUID.isPresent() && attunedToUUID.isPresent()) {
            if(!player.getUUID().equals(attunedToUUID.get())) return 0;

            return getLevelOfAttunementAchieved(attunedToUUID.get(), itemAttunementUUID.get());
        }
        return 0;
    }

    public static int getLevelOfAttunementAchieved(UUID playerUUID, UUID itemAttunementUUID) {
        return ArtifactorySavedData.get().getAttunedItem(playerUUID, itemAttunementUUID).map(AttunedItem::getAttunementLevel).orElse(0);
    }

    public static boolean doesPlayerHaveSlotCapacityToAttuneItem(Player player, ItemAttunementData attunementData) {
        int openSlots = getOpenAttunementSlots(player);
        int attunementSlotsRequired = attunementData.getAttunementSlotsUsed();
        boolean uniqueRestrictionActive = attunementData.unique() && AttunementUtil.isPlayerAtUniqueAttunementLimit(player.getUUID());
        return openSlots >= attunementSlotsRequired && !uniqueRestrictionActive;
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
        return !stack.isEmpty() && StackNBTUtil.containsAttunedToUUID(stack);
    }

    public static boolean isUseRestricted(Player player, ItemStack stack) {
        return DataPackUtil.getAttunementData(stack).map(itemAttunementData -> {
            boolean isAttunedToPlayer = arePlayerAndItemAttuned(player, stack);
            return !itemAttunementData.useWithoutAttunement() && !isAttunedToPlayer;
        }).orElse(false);
    }

    public static boolean isAttunedToAnotherPlayer(Player player, ItemStack stack) {
        return StackNBTUtil.getAttunedToUUID(stack).map(attunedToUUID -> !player.getUUID().equals(attunedToUUID)).orElse(false);
    }

    public static boolean arePlayerAndItemAttuned(Player player, ItemStack stack) {
        return isItemAttunedToPlayer(player, stack) && isPlayerAttunedToItem(player, stack);
    }

    public static boolean isItemAttunedToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty() || !StackNBTUtil.containsAttunedToUUID(stack)) return false;
        return StackNBTUtil.getAttunedToUUID(stack).map(attunedToUUID -> player.getUUID().equals(attunedToUUID)).orElse(false);
    }

    private static boolean isPlayerAttunedToItem(Player player, ItemStack stack) {
        return StackNBTUtil.getItemAttunementUUID(stack).map(itemAttunementUUID ->
                        ArtifactorySavedData.get().getAttunedItem(player.getUUID(), itemAttunementUUID).isPresent())
                .orElse(false);
    }

    public static boolean isAvailableToAttune(ItemStack stack) {
        boolean uniqueRestrictionActive = false;
        if(DataPackUtil.isUniqueAttunement(stack)) {
            uniqueRestrictionActive = !AttunementUtil.getPlayerUUIDsWithAttunementToItem(ResourceLocationUtil.getResourceLocation(stack)).isEmpty();
        }
        return isValidAttunementItem(stack) && !StackNBTUtil.containsAttunedToUUID(stack) && !uniqueRestrictionActive;
    }

    public static boolean isValidAttunementItem(ItemStack stack) {
        return !stack.isEmpty() && DataPackUtil.getAttunementData(stack).map(attunementData -> attunementData.attunementSlotsUsed() >= 0).orElse(false);
    }

    public static String getAttunedItemDisplayName(ItemStack stack) {
        return GUIUtil.prettifyName(StackNBTUtil.getDisplayNameFromNBT(stack).orElse(stack.getItem().toString()));
    }

    public static Optional<String> getSavedDataAttunedItemOwnerDisplayName(ItemStack stack) {
        return StackNBTUtil.getAttunedToUUID(stack).flatMap(attunedToUUID -> ArtifactorySavedData.get().getPlayerName(attunedToUUID));
    }

    public static boolean isPlayerAtUniqueAttunementLimit(UUID playerUUID) {
        return getPlayersNumberOfUniqueAttunements(playerUUID) >= Config.NUMBER_UNIQUE_ATTUNEMENTS_PER_PLAYER.get();
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
