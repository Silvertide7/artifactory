package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.client.utils.ClientAttunedItems;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.function.Supplier;


public class CB_UpdateAttunedItemMessage {
    private final AttunedItem attunedItem;

    public CB_UpdateAttunedItemMessage(AttunedItem itemToUpdate ) {
        this.attunedItem = itemToUpdate;
    }
    public CB_UpdateAttunedItemMessage(FriendlyByteBuf buf) {
        this(AttunedItem.decode(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        AttunedItem.encode(buf, attunedItem);
    }

    static void handle(CB_UpdateAttunedItemMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientAttunedItems.syncAttunedItem(msg.attunedItem);
        });
        context.setPacketHandled(true);
    }
}
