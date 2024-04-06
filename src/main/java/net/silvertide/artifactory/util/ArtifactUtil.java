package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.config.codecs.AttuneableItems;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.modifications.AttunementModificationFactory;
import net.silvertide.artifactory.registry.AttributeRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.Stack;
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
            numAttunementSlotsUsed += ArtifactUtil.getAttunementData(new ResourceLocation(attunedItem.resourceLocation())).map(ItemAttunementData::attunementSlotsUsed).orElse(0);
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
        getAttunementData(stack).ifPresent(attunementData -> {
            if (isAttunementAllowed(player, stack, attunementData)) {
                setupStackToAttune(stack);
                linkPlayerAndItem(player, stack);
                updateItemWithAttunementModifications(stack, attunementData, 1);
            }
        });
    }

//    public static void ascendItem(Player player, ItemStack stack){}

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
        AttunementModificationFactory.createAttunementModification(modificationString).ifPresent(attunementModification -> {
            attunementModification.applyModification(stack);
        });
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
        return getAttunementData(stack).map(itemAttunementData -> {
            boolean isAttunedToPlayer = ArtifactUtil.arePlayerAndItemAttuned(player, stack);
            return !itemAttunementData.useWithoutAttunement() && !isAttunedToPlayer;
        }).orElse(false);
    }

    public static boolean canUseWithoutAttunement(ItemStack stack) {
        return getAttunementData(stack).map(ItemAttunementData::useWithoutAttunement).orElse(false);
    }

    public static boolean isAttunedToAnotherPlayer(Player player, ItemStack stack) {
        return getAttunementData(stack).map(itemAttunementData ->
                ArtifactUtil.isItemAttuned(stack) && !ArtifactUtil.arePlayerAndItemAttuned(player, stack))
                .orElse(false);
    }

    public static boolean isPlayerAttunedToItem(Player player, ItemStack stack) {
        return StackNBTUtil.getItemAttunementUUID(stack).map(itemAttunementUUID ->
                ArtifactorySavedData.get().getAttunedItem(player.getUUID(), itemAttunementUUID).isPresent())
                .orElse(false);
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

    public static Optional<ItemAttunementData> getAttunementData(ResourceLocation resourceLocation) {
        return Optional.ofNullable(AttuneableItems.DATA_LOADER.getData().get(resourceLocation));
    }

    public static boolean hasAttunementData(ItemStack stack) {
        return getAttunementData(stack).isPresent();
    }

    public static void removeAttunement(ItemStack stack) {
        if(isItemAttuned(stack)) {
            StackNBTUtil.getAttunedToUUID(stack).ifPresent(playerUUID -> {
                StackNBTUtil.getItemAttunementUUID(stack).ifPresent(itemUUID -> {
                    ArtifactorySavedData.get().removeAttunedItem(playerUUID, itemUUID);
                    StackNBTUtil.removeArtifactoryTag(stack);
                });
            });
        }
    }
}
