package net.silvertide.artifactory.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.commands.CmdRoot;
import net.silvertide.artifactory.config.codecs.AttunableItems;
import net.silvertide.artifactory.network.client_packets.CB_SyncDatapackData;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.NetworkUtil;

import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Artifactory.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SystemEvents {
    @SubscribeEvent(priority=EventPriority.LOW)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            // Sync attuned items and information to player
            Map<UUID, AttunedItem> attunedItems = ArtifactorySavedData.get().getAttunedItems(serverPlayer.getUUID());
            NetworkUtil.updateAllAttunedItems(serverPlayer, attunedItems);
            ArtifactorySavedData.get().updatePlayerDisplayName(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CmdRoot.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onDatapackReload(OnDatapackSyncEvent event) {
        DataPackUtil.getAttunementDataMap().ifPresent(dataMap -> {
            event.getRelevantPlayers().forEach(serverPlayer ->
                    PacketDistributor.sendToPlayer(serverPlayer, new CB_SyncDatapackData(dataMap)));
        });
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event)
    {
        event.addListener(AttunableItems.DATA_LOADER);
    }
}
