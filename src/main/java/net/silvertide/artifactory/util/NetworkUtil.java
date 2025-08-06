package net.silvertide.artifactory.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.network.client_packets.CB_SyncServerConfigs;
import net.silvertide.artifactory.network.client_packets.CB_UpdateAttunedItem;
import net.silvertide.artifactory.registry.AttributeRegistry;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.Map;
import java.util.UUID;

public final class NetworkUtil {
    private NetworkUtil() {}

    public static void updateAllAttunedItems(ServerPlayer serverPlayer, Map<UUID, AttunedItem> attunedItems) {
        for(Map.Entry<UUID, AttunedItem> playersAttunedItems : attunedItems.entrySet()) {
            AttunedItem attunedItem = playersAttunedItems.getValue();
            PacketDistributor.sendToPlayer(serverPlayer, new CB_UpdateAttunedItem(attunedItem));
        }
    }

    public static void syncServerConfigs(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new CB_SyncServerConfigs(ServerConfigs.XP_LEVELS_TO_ATTUNE_THRESHOLD.get(), ServerConfigs.XP_LEVELS_TO_ATTUNE_CONSUMED.get()));
    }

    public static void updateAttunementLevelAttribute(ServerPlayer serverPlayer) {
        if(ServerConfigs.UPDATE_ATTUNEMENT_LEVEL_ATTRIBUTE.get()) {
            AttributeInstance attunementSlotsAttribute = serverPlayer.getAttribute(AttributeRegistry.ATTUNEMENT_SLOTS);
            if(attunementSlotsAttribute != null && attunementSlotsAttribute.getBaseValue() != ServerConfigs.BASE_ATTUNEMENT_LEVEL_ATTRIBUTE.get()) {
                attunementSlotsAttribute.setBaseValue(ServerConfigs.BASE_ATTUNEMENT_LEVEL_ATTRIBUTE.get());
            }
        }
    }
}
