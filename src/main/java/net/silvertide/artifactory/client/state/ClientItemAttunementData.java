package net.silvertide.artifactory.client.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.PlayerMessenger;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.Map;
import java.util.Optional;

public class ClientItemAttunementData {
    private static Map<ResourceLocation, AttunementDataSource> itemAttunementData = null;
    private ClientItemAttunementData() {}

    public static void setAttunementData(Map<ResourceLocation, AttunementDataSource> syncedAttunementData) {
        itemAttunementData = syncedAttunementData;
    }

    public static Optional<AttunementDataSource> getAttunementData(ResourceLocation resourceLocation) {
        if(itemAttunementData == null) return Optional.empty();
        return Optional.ofNullable(itemAttunementData.get(resourceLocation));
    }

    public static Optional<AttunementDataSource> getAttunementData(ItemStack stack) {
        if(itemAttunementData == null) return Optional.empty();
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return Optional.ofNullable(itemAttunementData.get(stackResourceLocation));
    }

    public static Optional<AttunementDataSource> getAttunementData(String resourceLocation) {
        if(itemAttunementData == null) return Optional.empty();
        return getAttunementData(ResourceLocation.parse(resourceLocation));
    }

    public static boolean isValidAttunementItem(ItemStack stack) {
        return !stack.isEmpty() && getAttunementData(stack).map(attunementData -> attunementData.attunementSlotsUsed() >= 0).orElse(false);
    }

    public static boolean isUseRestricted(Player player, ItemStack stack) {
        if(!isValidAttunementItem(stack)) return false;
        return getAttunementData(stack).map(itemAttunementData -> {
            if(AttunementUtil.isAttunedToAnotherPlayer(player, stack)) {
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
}
