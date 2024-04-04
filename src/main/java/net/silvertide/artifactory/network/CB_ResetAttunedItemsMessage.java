package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.client.utils.ClientAttunedItems;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.function.Supplier;


public class CB_ResetAttunedItemsMessage {

    public CB_ResetAttunedItemsMessage(AttunedItem itemToUpdate ) {}
    public CB_ResetAttunedItemsMessage(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    static void handle(CB_ResetAttunedItemsMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(ClientAttunedItems::clearAllAttunedItems);
        context.setPacketHandled(true);
    }
}
