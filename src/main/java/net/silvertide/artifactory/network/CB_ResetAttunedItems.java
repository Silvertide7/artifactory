package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.client.state.ClientAttunedItems;

import java.util.function.Supplier;


public class CB_ResetAttunedItems {
    public CB_ResetAttunedItems() {}
    public CB_ResetAttunedItems(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
    static void handle(CB_ResetAttunedItems msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(ClientAttunedItems::clearAllAttunedItems);
        context.setPacketHandled(true);
    }
}
