package net.silvertide.artifactory.util;

import net.minecraft.server.level.ServerPlayer;
import net.silvertide.artifactory.network.*;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;

import java.util.Map;
import java.util.UUID;

public final class NetworkUtil {
    private NetworkUtil() {}

    public static void updateAllAttunedItems(ServerPlayer serverPlayer, Map<UUID, AttunedItem> attunedItems) {
        for(Map.Entry<UUID, AttunedItem> playersAttunedItems : attunedItems.entrySet()) {
            AttunedItem attunedItem = playersAttunedItems.getValue();
            PacketHandler.sendToClient(serverPlayer, new CB_UpdateAttunedItem(attunedItem));
            updateAttunedItemModificationDescription(serverPlayer, attunedItem);
        }
    }

    public static void updateAttunedItemModificationDescription(ServerPlayer player, AttunedItem attunedItem) {
        String description = DataPackUtil.getAttunementLevelDescriptions(attunedItem.getResourceLocation(), attunedItem.getAttunementLevel());
        PacketHandler.sendToClient(player, new CB_UpdateAttunedItemModifications(description));
    }

    public static void syncClientAttunementNexusSlotInformation(ServerPlayer serverPlayer, AttunementNexusSlotInformation attunementNexusSlotInformation) {
        PacketHandler.sendToClient(serverPlayer, new CB_UpdateAttunementNexusSlotInformation(attunementNexusSlotInformation));
    }

    public static void syncAttunementData(ServerPlayer player) {
        DataPackUtil.getAttunementDataMap().ifPresent(dataMap -> {
            PacketHandler.sendToClient(player, new CB_UpdateAttunementData(dataMap));
        });
    }
}
