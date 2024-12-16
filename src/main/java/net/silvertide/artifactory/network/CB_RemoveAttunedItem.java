package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.client.util.ClientUtil;

import java.util.UUID;
import java.util.function.Supplier;


public class CB_RemoveAttunedItem {
    private final UUID itemUUIDToRemove;
    public CB_RemoveAttunedItem(UUID itemUUIDToRemove) {
        this.itemUUIDToRemove = itemUUIDToRemove;
    }
    public CB_RemoveAttunedItem(FriendlyByteBuf buf) {
        itemUUIDToRemove = buf.readUUID();
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(itemUUIDToRemove);
    }

    static void handle(CB_RemoveAttunedItem msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientUtil.removeAttunedItem(msg.itemUUIDToRemove);
        });
        context.setPacketHandled(true);
    }
}
