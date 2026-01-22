package net.silvertide.artifactory.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.commands.CmdRoot;
import net.silvertide.artifactory.config.codecs.AttunableItems;
import net.silvertide.artifactory.network.client_packets.CB_SyncDatapackData;
import net.silvertide.artifactory.services.AttunementService;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.AttunementDataSourceUtil;
import net.silvertide.artifactory.util.NetworkUtil;

import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Artifactory.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SystemEvents {
    @SubscribeEvent()
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.server.execute(() -> {
                if (serverPlayer.server.getPlayerList().getPlayer(serverPlayer.getUUID()) == null) return;
                ArtifactorySavedData.get().updatePlayerDisplayName(serverPlayer);

                AttunementService.clearBrokenAttunements(serverPlayer);

                NetworkUtil.syncServerConfigs(serverPlayer);
                NetworkUtil.updateAttunementLevelAttribute(serverPlayer);

                Map<UUID, AttunedItem> attunedItems = ArtifactorySavedData.get().getAttunedItems(serverPlayer.getUUID());
                NetworkUtil.updateAllAttunedItems(serverPlayer, attunedItems);
            });

        }
    }

    @SubscribeEvent
    public static void onTagLoad(TagsUpdatedEvent event) {
        AttunableItems.DATA_LOADER.postProcess(event.getRegistryAccess());
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CmdRoot.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onDatapackReload(OnDatapackSyncEvent event) {
        AttunementDataSourceUtil.getAttunementDataMap().ifPresent(dataMap -> {
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
