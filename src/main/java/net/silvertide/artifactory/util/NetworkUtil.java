package net.silvertide.artifactory.util;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.network.client_packets.CB_UpdateAttunedItem;
import net.silvertide.artifactory.network.client_packets.CB_UpdateAttunedItemModifications;
import net.silvertide.artifactory.network.client_packets.CB_UpdateAttunementNexusSlotInformation;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;

import java.util.Map;
import java.util.UUID;

public final class NetworkUtil {
    private NetworkUtil() {}

    public static void updateAllAttunedItems(ServerPlayer serverPlayer, Map<UUID, AttunedItem> attunedItems) {
        for(Map.Entry<UUID, AttunedItem> playersAttunedItems : attunedItems.entrySet()) {
            AttunedItem attunedItem = playersAttunedItems.getValue();
            PacketDistributor.sendToPlayer(serverPlayer, new CB_UpdateAttunedItem(attunedItem));
            updateAttunedItemModificationDescription(serverPlayer, attunedItem);
        }
    }

    public static void updateAttunedItemModificationDescription(ServerPlayer player, AttunedItem attunedItem) {
        String description;
        if(attunedItem.getAttunementOverride() != null) {
            description = AttunementSchemaUtil.getAttunementLevelDescriptions(attunedItem.getAttunementOverride(), attunedItem.getResourceLocation(), attunedItem.getAttunementLevel());
        } else {
            description = AttunementDataSourceUtil.getAttunementDataSource(attunedItem.getResourceLocation())
                    .map(attunementDataSource -> AttunementSchemaUtil.getAttunementLevelDescriptions(attunementDataSource, attunedItem.getResourceLocation(), attunedItem.getAttunementLevel()))
                    .orElse("");
        }
        PacketDistributor.sendToPlayer(player, new CB_UpdateAttunedItemModifications(description));
    }

    public static void syncClientAttunementNexusSlotInformation(ServerPlayer serverPlayer, AttunementNexusSlotInformation attunementNexusSlotInformation) {
        PacketDistributor.sendToPlayer(serverPlayer, new CB_UpdateAttunementNexusSlotInformation(attunementNexusSlotInformation));
    }
}
