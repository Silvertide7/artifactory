package net.silvertide.artifactory.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.capabilities.AttunedItem;
import net.silvertide.artifactory.capabilities.AttunedItems;
import net.silvertide.artifactory.config.codecs.AttuneableItems;
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

    public static int getOpenAttunementSlots(Player player) {
        int maxAttunementSlots = (int) player.getAttributeValue(AttributeRegistry.MAX_ATTUNEMENT_SLOTS.get());

        int slotsUsed = CapabilityUtil.getAttunedItems(player).resolve().map(AttunedItems::getNumAttunedItems).orElse(0);
        return maxAttunementSlots - slotsUsed;
    }

    public static boolean canPlayerAttuneItem(Player player, ItemAttunementData attunementData) {
        return getOpenAttunementSlots(player) >= attunementData.attunementSlotsUsed();
    }

    public static boolean isAttunementAllowed(Player player, ItemStack stack, ItemAttunementData attunementDatat) {
        return !stack.isEmpty() && isAttuneable(stack) && canPlayerAttuneItem(player, attunementDatat);
    }

    public static void attuneItem(Player player, ItemStack stack) {
        Optional<ItemAttunementData> itemAttunementData = getAttunementData(stack);
        itemAttunementData.ifPresent( attunementData -> {
            if(isAttunementAllowed(player, stack, attunementData)) {
                setupStack(stack);
                AttunedItem.buildAttunedItem(stack, attunementData).ifPresent(attunedItem -> {
                    boolean succeeded = addAttunementToPlayer(player, attunedItem);
                    if (succeeded) addAttunementToStack(player, stack);
                });
            }
        });
    }

    private static void setupStack(ItemStack stack) {
        if(NBTUtil.artifactoryTagExists(stack)) NBTUtil.removeArtifactoryTag(stack);
        NBTUtil.setItemAttunementUUID(stack, UUID.randomUUID());
    }

    private static boolean addAttunementToPlayer(Player player, AttunedItem attunedItem) {
        return CapabilityUtil.getAttunedItems(player).map(attunedItemsCap -> {
            attunedItemsCap.addAttunedItem(attunedItem);
            return true;
        }).orElse(false);
    }

    private static void addAttunementToStack(Player player, ItemStack stack) {
        NBTUtil.putPlayerDataInArtifactoryTag(player, stack);
    }

    public static boolean isItemAttunedToPlayer(Player player, ItemStack stack) {
        if(stack.isEmpty() || !NBTUtil.containsAttunedToUUID(stack)) return false;
        return NBTUtil.getAttunedToUUID(stack).map(attunedToUUID -> player.getUUID().equals(attunedToUUID)).orElse(false);
    }

    public static boolean isPlayerAttunedToItem(Player player, ItemStack stack) {
        return CapabilityUtil.getAttunedItems(player).map(attunedItemsCap -> {
                Optional<UUID> itemAttunementUUID = NBTUtil.getItemAttunementUUID(stack);
                if(itemAttunementUUID.isPresent()) {
                    return attunedItemsCap.getAttunedItem(itemAttunementUUID.get()).isPresent();
                } else {
                    return false;
                }
        }).orElse(false);
    }

    public static boolean arePlayerAndStackAttuned(Player player, ItemStack stack) {
        return isItemAttunedToPlayer(player, stack) && isPlayerAttunedToItem(player, stack);
    }

    public static boolean isAttuneable(ItemStack stack) {
        // TODO: Might want to check if the player still has the item attuned here and break the connection if not.
        return !stack.isEmpty() && !NBTUtil.containsAttunedToUUID(stack) && hasAttunementData(stack);
    }

    public static Optional<ItemAttunementData> getAttunementData(ItemStack stack) {
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        Map<ResourceLocation, ItemAttunementData> itemAttunementDataMap = AttuneableItems.DATA_LOADER.getData();

        if(itemAttunementDataMap.containsKey(stackResourceLocation)) {
            return Optional.of(itemAttunementDataMap.get(stackResourceLocation));
        } else {
            return Optional.empty();
        }
    }

    public static boolean hasAttunementData(ItemStack stack) {
        return getAttunementData(stack).isPresent();
    }

    public static void displayClientMessage(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    public static void sendSystemMessage(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

}
