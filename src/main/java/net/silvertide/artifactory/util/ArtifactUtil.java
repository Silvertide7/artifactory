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

public final class ArtifactUtil {

    private ArtifactUtil() {}

    public static int getOpenAttunementSlots(Player player) {
        int maxAttunementSlots = (int) player.getAttributeValue(AttributeRegistry.MAX_ATTUNEMENT_SLOTS.get());

        int slotsUsed = CapabilityUtil.getAttunedItems(player).resolve().map(AttunedItems::getNumAttunedItems).orElse(0);
        return maxAttunementSlots - slotsUsed;
    }

    public static boolean canPlayerAttuneItem(Player player, ItemStack stack) {
        Optional<ItemAttunementData> attunementData = getAttunementData(stack);
        return attunementData.map(itemAttunementData -> {
            int numSlotsToAttune = attunementData.get().attunementSlotsUsed();
            return getOpenAttunementSlots(player) >= numSlotsToAttune;
        }).orElse(false);
    }

    public static int getMaxAttunementSlots(Player player) {
        return (int) player.getAttributeValue(AttributeRegistry.MAX_ATTUNEMENT_SLOTS.get());
    }

    public static boolean isAttunementAllowed(Player player, ItemStack stack) {
        return isAttuneable(stack) && canPlayerAttuneItem(player, stack);
    }

    public static void attuneItem(Player player, ItemStack stack) {
        if(isAttunementAllowed(player, stack)) {
            if(addAttunementToPlayer(player, stack)){
                addAttunementToItem(player, stack);
            }
        }
    }

    public static boolean addAttunementToPlayer(Player player, ItemStack stack) {
        return CapabilityUtil.getAttunedItems(player).map(attunedItemsCap -> {
            Optional<ItemAttunementData> attunementData = getAttunementData(stack);
            if(attunementData.isPresent()) {
                AttunedItem attunedItem = AttunedItem.buildAttunedItem(stack, attunementData.get());
                attunedItemsCap.attuneItem(attunedItem);
                return true;
            } else {
                return false;
            }
        }).orElse(false);
    }

    public static void addAttunementToItem(Player player, ItemStack stack) {

    }

    public static boolean isAttuneable(ItemStack stack) {
        return hasAttunementData(stack) && !NBTUtil.hasArtifactoryTag(stack, NBTUtil.ATTUNED_TO_UUID_NBT_KEY);
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
