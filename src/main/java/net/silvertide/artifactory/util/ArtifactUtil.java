package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.modifications.AttunementModification;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.registry.AttributeRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ArtifactUtil {
    private ArtifactUtil() {}

    public static int getMaxAttunementSlots(Player player) {
        return (int) player.getAttributeValue(AttributeRegistry.MAX_ATTUNEMENT_SLOTS.get());
    }

    public static int getAttunementSlotsUsed(Player player) {
        Map<UUID, AttunedItem> attunedItems = ArtifactorySavedData.get().getAttunedItems(player.getUUID());
        int numAttunementSlotsUsed = 0;
        for(AttunedItem attunedItem : attunedItems.values()) {
            numAttunementSlotsUsed += AttunementDataUtil.getAttunementData(new ResourceLocation(attunedItem.resourceLocation())).map(ItemAttunementData::attunementSlotsUsed).orElse(0);
        }
        return numAttunementSlotsUsed;
    }

    public static int getOpenAttunementSlots(Player player) {
        return getMaxAttunementSlots(player) - getAttunementSlotsUsed(player);
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
        AttunementDataUtil.getAttunementData(stack).ifPresent(attunementData -> {
            if (isAttunementAllowed(player, stack, attunementData)) {
                setupStackToAttune(stack);
                linkPlayerAndItem(player, stack);
                updateItemWithAttunementModifications(stack, attunementData, 1);
            }
        });
    }

    private static void setupStackToAttune(ItemStack stack) {
        if (StackNBTUtil.artifactoryTagExists(stack)) StackNBTUtil.removeArtifactoryTag(stack);
        StackNBTUtil.setItemAttunementUUID(stack, UUID.randomUUID());
    }

    public static void linkPlayerAndItem(Player player, ItemStack stack) {
        AttunedItem.buildAttunedItem(player, stack).ifPresent(attunedItem -> {
            addAttunementToPlayer(player, attunedItem);
            addAttunementToStack(player, stack);
        });
    }

    public static void updateItemWithAttunementModifications(ItemStack stack, ItemAttunementData attunementData, int level) {
        if (attunementData.attunements().containsKey(String.valueOf(level))) {
            AttunementLevel attunementLevel = attunementData.attunements().get(String.valueOf(level));
            for (String modification : attunementLevel.modifications()) {
                applyAttunementModification(stack, modification);
            }
        }
    }

    public static void applyAttunementModification(ItemStack stack, String modificationString) {
        // TODO: This is where it is breaking, it creates an attribute modification but returns a null attunement modification
        AttunementModification attunementModification = AttunementModificationUtil.createAttunementModification(modificationString);
        if (attunementModification != null) {
            attunementModification.applyModification(stack);
        }
    }

    private static void addAttunementToPlayer(Player player, AttunedItem attunedItem) {
        ArtifactorySavedData.get().setAttunedItem(player.getUUID(), attunedItem);
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

    public static boolean isUseRestricted(Player player, ItemStack stack) {
        return AttunementDataUtil.getAttunementData(stack).map(itemAttunementData -> {
            boolean isAttunedToPlayer = arePlayerAndItemAttuned(player, stack);
            return !itemAttunementData.useWithoutAttunement() && !isAttunedToPlayer;
        }).orElse(false);
    }

    public static boolean canUseWithoutAttunement(ItemStack stack) {
        return AttunementDataUtil.getAttunementData(stack).map(ItemAttunementData::useWithoutAttunement).orElse(false);
    }

    public static boolean isAttunedToAnotherPlayer(Player player, ItemStack stack) {
        return AttunementDataUtil.getAttunementData(stack).map(itemAttunementData ->
                isItemAttuned(stack) && !arePlayerAndItemAttuned(player, stack))
                .orElse(false);
    }

    public static boolean isPlayerAttunedToItem(Player player, ItemStack stack) {
        return StackNBTUtil.getItemAttunementUUID(stack).map(itemAttunementUUID ->
                ArtifactorySavedData.get().getAttunedItem(player.getUUID(), itemAttunementUUID).isPresent())
                .orElse(false);
    }

    public static boolean arePlayerAndItemAttuned(Player player, ItemStack stack) {
        if (isItemAttunedToPlayer(player, stack)) {
            if(isPlayerAttunedToItem(player, stack)) {
                return true;
            }

            // If the item is attuned to the player but the player is no longer attuned to the item remove the
            // items attunement data to sync it.
            removeAttunement(stack);
        }
        return false;
    }

    public static boolean isAvailableToAttune(ItemStack stack) {
        // TODO: Might want to check if the player still has the item attuned here and break the connection if not.
        return isAttuneableItem(stack) && !StackNBTUtil.containsAttunedToUUID(stack);
    }

    public static boolean isAttuneableItem(ItemStack stack) {
        return !stack.isEmpty() && AttunementDataUtil.hasAttunementData(stack);
    }

    public static int getLevelOfAttunementAchieved(ItemStack stack) {
        Optional<UUID> attunedToUUID = StackNBTUtil.getAttunedToUUID(stack);
        Optional<UUID> itemAttunementUUID = StackNBTUtil.getItemAttunementUUID(stack);

        if(itemAttunementUUID.isPresent() && attunedToUUID.isPresent()) {
            return ArtifactorySavedData.get().getAttunedItem(attunedToUUID.get(), itemAttunementUUID.get()).map(AttunedItem::attunementLevel).orElse(0);
        }

        return 0;
    }

    public static void removeAttunement(ItemStack stack) {
        Optional<UUID> attunedToUUID = StackNBTUtil.getAttunedToUUID(stack);
        Optional<UUID> itemAttunementUUID = StackNBTUtil.getItemAttunementUUID(stack);

        if(itemAttunementUUID.isPresent() && attunedToUUID.isPresent()) {
            ArtifactorySavedData artifactorySavedData = ArtifactorySavedData.get();
            int currentAttunementLevel = artifactorySavedData.getAttunedItem(attunedToUUID.get(), itemAttunementUUID.get()).map(AttunedItem::attunementLevel).orElse(0);
            if (AttunementModificationUtil.hasModification(stack, currentAttunementLevel, "unbreakable")) {
                StackNBTUtil.removeUnbreakable(stack);
            }
            artifactorySavedData.removeAttunedItem(attunedToUUID.get(), itemAttunementUUID.get());
        }

        StackNBTUtil.removeArtifactoryTag(stack);
    }
}
