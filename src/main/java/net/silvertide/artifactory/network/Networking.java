package net.silvertide.artifactory.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.network.client_packets.*;
import net.silvertide.artifactory.network.server_packets.SB_RemoveAttunedItem;
import net.silvertide.artifactory.network.server_packets.SB_ToggleManageAttunementsScreen;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Artifactory.MOD_ID)
public class Networking {
    @SubscribeEvent
    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Artifactory.MOD_ID);

        registrar
                // CLIENT BOUND PACKETS
                .playToClient(CB_OpenManageAttunementsScreen.TYPE, CB_OpenManageAttunementsScreen.STREAM_CODEC, CB_OpenManageAttunementsScreen::handle)
                .playToClient(CB_RemoveAttunedItem.TYPE, CB_RemoveAttunedItem.STREAM_CODEC, CB_RemoveAttunedItem::handle)
                .playToClient(CB_ResetAttunedItems.TYPE, CB_ResetAttunedItems.STREAM_CODEC, CB_ResetAttunedItems::handle)
                .playToClient(CB_UpdateAttunedItem.TYPE, CB_UpdateAttunedItem.STREAM_CODEC, CB_UpdateAttunedItem::handle)
                .playToClient(CB_SyncDatapackData.TYPE, CB_SyncDatapackData.STREAM_CODEC, CB_SyncDatapackData::handle)
                .playToClient(CB_SyncServerConfigs.TYPE, CB_SyncServerConfigs.STREAM_CODEC, CB_SyncServerConfigs::handle)
                // SERVER BOUND PACKETS
                .playToServer(SB_RemoveAttunedItem.TYPE, SB_RemoveAttunedItem.STREAM_CODEC, SB_RemoveAttunedItem::handle)
                .playToServer(SB_ToggleManageAttunementsScreen.TYPE, SB_ToggleManageAttunementsScreen.STREAM_CODEC, SB_ToggleManageAttunementsScreen::handle);
    }
}
