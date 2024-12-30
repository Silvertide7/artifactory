package net.silvertide.artifactory.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.network.client_packets.*;
import net.silvertide.artifactory.network.server_packets.SB_RemoveAttunedItem;
import net.silvertide.artifactory.network.server_packets.SB_ToggleManageAttunementsScreen;

public class Networking {
    @SubscribeEvent
    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Artifactory.MOD_ID);

        registrar
                // CLIENT BOUND PACKETS
                .playToClient(CB_OpenManageAttunementsScreen.TYPE, CB_OpenManageAttunementsScreen.CODEC, CB_OpenManageAttunementsScreen::handle)
                .playToClient(CB_RemoveAttunedItem.TYPE, CB_RemoveAttunedItem.CODEC, CB_RemoveAttunedItem::handle)
                .playToClient(CB_ResetAttunedItems.TYPE, CB_ResetAttunedItems.CODEC, CB_ResetAttunedItems::handle)
                .playToClient(CB_UpdateAttunedItem.TYPE, CB_UpdateAttunedItem.CODEC, CB_UpdateAttunedItem::handle)
                .playToClient(CB_UpdateAttunedItemModifications.TYPE, CB_UpdateAttunedItemModifications.CODEC, CB_UpdateAttunedItemModifications::handle)
                .playToClient(CB_UpdateAttunementNexusSlotInformation.TYPE, CB_UpdateAttunementNexusSlotInformation.CODEC, CB_UpdateAttunementNexusSlotInformation::handle)
                // SERVER BOUND PACKETS
                .playToServer(SB_RemoveAttunedItem.TYPE, SB_RemoveAttunedItem.CODEC, SB_RemoveAttunedItem::handle)
                .playToServer(SB_ToggleManageAttunementsScreen.TYPE, SB_ToggleManageAttunementsScreen.CODEC, SB_ToggleManageAttunementsScreen::handle);
    }

    public static void registerDataSyncPackets() {
//        Core.get(LogicalSide.SERVER).getLoader().RELOADER.subscribeAsSyncable(CP_ClearData::new);
//        Config.CONFIG.subscribeAsSyncable();
    }
}
