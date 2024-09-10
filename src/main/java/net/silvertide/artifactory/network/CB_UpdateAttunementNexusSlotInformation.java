package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.client.utils.ClientAttunementNexusSlotInformation;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;

import java.util.function.Supplier;


public class CB_UpdateAttunementNexusSlotInformation {
    private final AttunementNexusSlotInformation attunedNexusSlotInformation;

    public CB_UpdateAttunementNexusSlotInformation(AttunementNexusSlotInformation slotInformation ) {
        this.attunedNexusSlotInformation = slotInformation;
    }

    public CB_UpdateAttunementNexusSlotInformation(FriendlyByteBuf buf) {
        this(AttunementNexusSlotInformation.decode(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        AttunementNexusSlotInformation.encode(buf, attunedNexusSlotInformation);
    }

    static void handle(CB_UpdateAttunementNexusSlotInformation msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientAttunementNexusSlotInformation.setSlotInformation(msg.attunedNexusSlotInformation);
        });
        context.setPacketHandled(true);
    }
}
