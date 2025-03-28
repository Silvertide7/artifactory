package net.silvertide.artifactory.client.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.AttunementOverride;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.util.AttunementDataSourceUtil;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.services.PlayerMessenger;
import net.silvertide.artifactory.util.DataComponentUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.Map;
import java.util.Optional;

public class ClientItemAttunementData {
    private static Map<ResourceLocation, AttunementDataSource> itemAttunementData = null;
    private ClientItemAttunementData() {}

    public static void setAttunementData(Map<ResourceLocation, AttunementDataSource> syncedAttunementData) {
        itemAttunementData = syncedAttunementData;
    }

    public static Optional<AttunementSchema> getClientAttunementSchema(ItemStack stack) {
        Optional<AttunementOverride> override = DataComponentUtil.getAttunementOverride(stack);
        if(override.isPresent()) return Optional.of(override.get());

        Optional<AttunementDataSource> clientSource = getClientAttunementDataSource(stack);
        if(clientSource.isPresent()) return Optional.of(clientSource.get());

        return Optional.empty();
    }

    public static Optional<AttunementDataSource> getClientAttunementDataSource(ResourceLocation resourceLocation) {
        if(itemAttunementData == null) return Optional.empty();
        return Optional.ofNullable(itemAttunementData.get(resourceLocation));
    }

    public static Optional<AttunementDataSource> getClientAttunementDataSource(ItemStack stack) {
        if(itemAttunementData == null) return Optional.empty();
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return getClientAttunementDataSource(stackResourceLocation);
    }

    public static Optional<AttunementDataSource> getClientAttunementDataSource(String resourceLocation) {
        if(itemAttunementData == null) return Optional.empty();
        return getClientAttunementDataSource(ResourceLocation.parse(resourceLocation));
    }

    public static boolean isValidAttunementItem(ItemStack stack) {
        return !stack.isEmpty() && getClientAttunementSchema(stack).map(AttunementSchema::isValidSchema).orElse(false);
    }

    public static boolean isUseRestricted(Player player, ItemStack stack) {
        if(!isValidAttunementItem(stack)) return false;
        return getClientAttunementDataSource(stack).map(itemAttunementData -> {

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
