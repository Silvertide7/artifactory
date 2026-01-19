package net.silvertide.artifactory.client.state;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.Map;
import java.util.Optional;

public class ClientItemAttunementData {
    private static Map<ResourceLocation, ItemAttunementData> itemAttunementData = null;
    private ClientItemAttunementData() {}

    public static void setAttunementData(Map<ResourceLocation, ItemAttunementData> syncedAttunementData) {
        itemAttunementData = syncedAttunementData;
    }

    public static Optional<ItemAttunementData> getAttunementData(ResourceLocation resourceLocation) {
        if(itemAttunementData == null) return Optional.empty();
        return Optional.ofNullable(itemAttunementData.get(resourceLocation));
    }

    public static Optional<ItemAttunementData> getAttunementData(ItemStack stack) {
        if(itemAttunementData == null) return Optional.empty();
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return Optional.ofNullable(itemAttunementData.get(stackResourceLocation));
    }

    public static Optional<ItemAttunementData> getAttunementData(String resourceLocation) {
        if(itemAttunementData == null) return Optional.empty();
        return getAttunementData(ResourceLocationUtil.getResourceLocation(resourceLocation));
    }

    public static boolean isValidAttunementItem(ItemStack stack) {
        return !stack.isEmpty() && getAttunementData(stack).map(attunementData -> attunementData.attunementSlotsUsed() >= 0).orElse(false);
    }

    public static boolean isUseRestricted(LocalPlayer player, ItemStack stack) {
        if(!isValidAttunementItem(stack)) return false;
        return getAttunementData(stack).map(itemAttunementData -> {
            if(AttunementUtil.isAttunedToAnotherPlayer(player, stack)) {
                return true;
            } else if(!AttunementUtil.isItemAttunedToPlayer(player, stack) && !itemAttunementData.useWithoutAttunement()) {
                return true;
            }
            return false;
        }).orElse(false);
    }
}
