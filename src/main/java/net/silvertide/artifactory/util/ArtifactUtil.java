package net.silvertide.artifactory.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.capabilities.AttunedItems;
import net.silvertide.artifactory.registry.AttributeRegistry;

public final class ArtifactUtil {
    public static final String ATTUNEMENT_ID_NBT_KEY = "attunement_id";
    public static final String ATTUNED_TO_NBT_KEY = "attuned_to";
    private ArtifactUtil() {}

    public int getOpenAttunementSlots(Player player) {
        int maxAttunementSlots = (int) player.getAttributeValue(AttributeRegistry.MAX_ATTUNEMENT_SLOTS.get());

        int slotsUsed = CapabilityUtil.getAttunedItems(player).resolve().map(AttunedItems::getNumAttunedItems).orElse(0);
        return maxAttunementSlots - slotsUsed;
    }



    public boolean isAttuneable(ItemStack stack) {
        CompoundTag stackNBT = stack.getOrCreateTag();
        return stackNBT.contains(ATTUNEMENT_ID_NBT_KEY) && !stackNBT.contains(ATTUNED_TO_NBT_KEY);
    }

    public void attuneItem(Player player, ItemStack stack) {
        if(isAttuneable(stack)) {

        }
    }
    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(Artifactory.MOD_ID, path);
    }


    public static void displayClientMessage(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    public static void sendSystemMessage(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

}
