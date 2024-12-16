package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.client.state.ClientAttunedItems;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.function.Supplier;


public class CB_UpdateAttunedItem {
    private final AttunedItem attunedItem;

    public CB_UpdateAttunedItem(AttunedItem itemToUpdate) {
        this.attunedItem = itemToUpdate;
    }
    public CB_UpdateAttunedItem(FriendlyByteBuf buf) {
        this(AttunedItem.decode(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        AttunedItem.encode(buf, attunedItem);
    }

    static void handle(CB_UpdateAttunedItem msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientAttunedItems.setAttunedItem(msg.attunedItem);
        });
        context.setPacketHandled(true);
    }
}
