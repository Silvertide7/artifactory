package net.silvertide.artifactory.util;

import net.minecraft.server.level.ServerPlayer;
import net.silvertide.artifactory.network.CB_UpdateAttunedItem;
import net.silvertide.artifactory.network.PacketHandler;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.Map;
import java.util.UUID;

public class NetworkUtil {
    private NetworkUtil() {}

    public static void updateAllAttunedItems(ServerPlayer serverPlayer, Map<UUID, AttunedItem> attunedItems) {
        for(Map.Entry<UUID, AttunedItem> playersAttunedItems : attunedItems.entrySet()) {
            PacketHandler.sendToClient(serverPlayer, new CB_UpdateAttunedItem(playersAttunedItems.getValue()));
        }
    }
}
