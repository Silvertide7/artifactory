package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.client.utils.ClientAttunedItems;

import java.util.function.Supplier;


public class CB_UpdateAttunedItemModifications {
    private final String attunedItemModifications;

    public CB_UpdateAttunedItemModifications(String attunedItemDescription ) {
        this.attunedItemModifications = attunedItemDescription;
    }
    public CB_UpdateAttunedItemModifications(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(attunedItemModifications);
    }

    static void handle(CB_UpdateAttunedItemModifications msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            String[] modificationInformation = msg.attunedItemModifications.split(";");
            if(modificationInformation.length == 2) {
                String resourceLocation = modificationInformation[0];
                String modificationDescription = modificationInformation[1];
                ClientAttunedItems.setModification(resourceLocation, modificationDescription);
            }
        });
        context.setPacketHandled(true);
    }
}
