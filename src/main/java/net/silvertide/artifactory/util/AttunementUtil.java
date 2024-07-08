package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.registry.AttributeRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
            numAttunementSlotsUsed += DataPackUtil.getAttunementData(new ResourceLocation(attunedItem.resourceLocation())).map(ItemAttunementData::attunementSlotsUsed).orElse(0);
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
            return ArtifactorySavedData.get().getAttunedItem(attunedToUUID.get(), itemAttunementUUID.get()).map(AttunedItem::attunementLevel).orElse(0);
        }
        return 0;
    }

    public static boolean canPlayerAttuneItem(Player player, ItemAttunementData attunementData) {
        int openSlots = getOpenAttunementSlots(player);
        int attunementSlotsRequired = attunementData.getAttunementSlotsUsed();
        return openSlots >= attunementSlotsRequired;
    }

    public static boolean canIncreaseAttunementLevel(Player player, ItemStack stack) {
        if(stack.isEmpty()) return false;
        return DataPackUtil.getAttunementData(stack).map(attunementData -> {
            if(isItemAttunedToPlayer(player, stack)) {
                int levelAchieved = getLevelOfAttunementAchieved(stack);
                int maxLevel = DataPackUtil.getMaxLevelOfAttunementPossible(stack);
                return levelAchieved < maxLevel;
            } else {
                return isAvailableToAttune(stack) && canPlayerAttuneItem(player, attunementData);
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

    public static boolean canUseWithoutAttunement(ItemStack stack) {
        return DataPackUtil.getAttunementData(stack).map(ItemAttunementData::useWithoutAttunement).orElse(false);
    }

    public static boolean isAttunedToAnotherPlayer(Player player, ItemStack stack) {
        return DataPackUtil.getAttunementData(stack).map(itemAttunementData ->
                        isItemAttunedToAPlayer(stack) && !arePlayerAndItemAttuned(player, stack))
                .orElse(false);
    }

    public static boolean arePlayerAndItemAttuned(Player player, ItemStack stack) {
        if (isItemAttunedToPlayer(player, stack)) {
            if(isPlayerAttunedToItem(player, stack)) {
                return true;
            }

            // If the item is attuned to the player but the player is no longer attuned to the item remove the
            // items attunement data to sync it.
            AttunementService.removeAttunementFromPlayerAndItem(stack);
        }
        return false;
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
        // TODO: Might want to check if the player still has the item attuned here and break the connection if not.
        return isAttunementItem(stack) && !StackNBTUtil.containsAttunedToUUID(stack);
    }

    public static boolean isAttunementItem(ItemStack stack) {
        return !stack.isEmpty() && DataPackUtil.hasAttunementData(stack);
    }
}
