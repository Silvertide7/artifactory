package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.capabilities.AttunedItem;
import net.silvertide.artifactory.capabilities.AttunedItems;
import net.silvertide.artifactory.config.codecs.AttuneableItems;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.modifications.AttunementModificationFactory;
import net.silvertide.artifactory.registry.AttributeRegistry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ArtifactUtil {

    private ArtifactUtil() {
    }

    public static int getMaxAttunementSlots(Player player) {
        return (int) player.getAttributeValue(AttributeRegistry.MAX_ATTUNEMENT_SLOTS.get());
    }

    public static int getOpenAttunementSlots(Player player) {
        int maxAttunementSlots = (int) player.getAttributeValue(AttributeRegistry.MAX_ATTUNEMENT_SLOTS.get());

        int slotsUsed = CapabilityUtil.getAttunedItems(player).map(AttunedItems::getNumAttunedItems).orElse(0);
        return maxAttunementSlots - slotsUsed;
    }

    public static boolean canPlayerAttuneItem(Player player, ItemAttunementData attunementData) {
        int openSlots = getOpenAttunementSlots(player);
        int attunementSlotsRequired = attunementData.getAttunementSlotsUsed();
        return openSlots >= attunementSlotsRequired;
    }

    public static boolean isAttunementAllowed(Player player, ItemStack stack, ItemAttunementData attunementData) {
        boolean attuneable = isAvailableToAttune(stack);
        boolean canPlayerAttune = canPlayerAttuneItem(player, attunementData);
        return !stack.isEmpty() && attuneable && canPlayerAttune;
    }

    public static void attuneItem(Player player, ItemStack stack) {
        getAttunementData(stack).ifPresent(attunementData -> {
            if (isAttunementAllowed(player, stack, attunementData)) {
                setupStackToAttune(stack);
                linkPlayerAndItem(player, stack, attunementData);
                //TODO: This is hardcoded to level 1 for now. Eventually this will need to be dynamic
                updateItemWithAttunementModifications(stack, attunementData, 1);
            }
        });
    }

    private static void setupStackToAttune(ItemStack stack) {
        if (StackNBTUtil.artifactoryTagExists(stack)) StackNBTUtil.removeArtifactoryTag(stack);
        StackNBTUtil.setItemAttunementUUID(stack, UUID.randomUUID());
    }

    public static void linkPlayerAndItem(Player player, ItemStack stack, ItemAttunementData attunementData) {
        AttunedItem.buildAttunedItem(stack, attunementData).ifPresent(attunedItem -> {
            boolean succeeded = addAttunementToPlayer(player, attunedItem);
            if (succeeded) addAttunementToStack(player, stack);
        });
    }

    public static void updateItemWithAttunementModifications(ItemStack stack, ItemAttunementData attunementData, int level) {
        if (attunementData.modifications().containsKey(String.valueOf(level))) {
            List<String> modifications = attunementData.modifications().get(String.valueOf(level));
            for (String modification : modifications) {
                applyAttunementModification(stack, modification);
            }
        }
    }

    public static void applyAttunementModification(ItemStack stack, String modificationString) {
        AttunementModificationFactory.createAttunementModification(modificationString).ifPresent(attunementModification -> {
            attunementModification.applyModification(stack);
        });
    }

    private static boolean addAttunementToPlayer(Player player, AttunedItem attunedItem) {
        return CapabilityUtil.getAttunedItems(player).map(attunedItemsCap -> {
            attunedItemsCap.addAttunedItem(attunedItem);
            return true;
        }).orElse(false);
    }

    private static void addAttunementToStack(Player player, ItemStack stack) {
        StackNBTUtil.putPlayerDataInArtifactoryTag(player, stack);
    }

    public static boolean isItemAttunedToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty() || !StackNBTUtil.containsAttunedToUUID(stack)) return false;
        return StackNBTUtil.getAttunedToUUID(stack).map(attunedToUUID -> player.getUUID().equals(attunedToUUID)).orElse(false);
    }

    public static boolean isItemAttuned(ItemStack stack) {
        return !stack.isEmpty() && StackNBTUtil.containsAttunedToUUID(stack);
    }

    public static boolean isItemUseable(Player player, ItemStack stack) {
        return getAttunementData(stack).map(itemAttunementData -> {
            boolean canUseWithoutAttunement = itemAttunementData.useWithoutAttunement();
            boolean isAttuned = ArtifactUtil.arePlayerAndItemAttuned(player, stack);
            return canUseWithoutAttunement || isAttuned;
        }).orElse(true);
    }

    public static boolean isPlayerAttunedToItem(Player player, ItemStack stack) {
        return CapabilityUtil.getAttunedItems(player).map(attunedItemsCap -> {
            Optional<UUID> itemAttunementUUID = StackNBTUtil.getItemAttunementUUID(stack);
            return itemAttunementUUID.filter(uuid -> attunedItemsCap.getAttunedItem(uuid).isPresent()).isPresent();
        }).orElse(false);
    }

    public static boolean arePlayerAndItemAttuned(Player player, ItemStack stack) {
        return isItemAttunedToPlayer(player, stack) && isPlayerAttunedToItem(player, stack);
    }

    public static boolean isAvailableToAttune(ItemStack stack) {
        // TODO: Might want to check if the player still has the item attuned here and break the connection if not.
        return isAttuneableItem(stack) && !StackNBTUtil.containsAttunedToUUID(stack);
    }

    public static boolean isAttuneableItem(ItemStack stack) {
        return !stack.isEmpty() && hasAttunementData(stack);
    }

    public static Optional<ItemAttunementData> getAttunementData(ItemStack stack) {
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return Optional.ofNullable(AttuneableItems.DATA_LOADER.getData().get(stackResourceLocation));
    }

    public static boolean hasAttunementData(ItemStack stack) {
        return getAttunementData(stack).isPresent();
    }
}
