package net.silvertide.artifactory.network.client_packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunementNexusSlotInformation;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;
import org.jetbrains.annotations.NotNull;

public record CB_UpdateAttunementNexusSlotInformation(AttunementNexusSlotInformation attunedNexusSlotInformation) implements CustomPacketPayload {
    public static final Type<CB_UpdateAttunementNexusSlotInformation> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_update_slot_information"));
    public static final StreamCodec<FriendlyByteBuf, CB_UpdateAttunementNexusSlotInformation> STREAM_CODEC = StreamCodec
            .composite(AttunementNexusSlotInformation.STREAM_CODEC, CB_UpdateAttunementNexusSlotInformation::attunedNexusSlotInformation, CB_UpdateAttunementNexusSlotInformation::new);

    public static void handle(CB_UpdateAttunementNexusSlotInformation packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientAttunementNexusSlotInformation.setSlotInformation(packet.attunedNexusSlotInformation));
    }
    @Override
    public @NotNull Type<CB_UpdateAttunementNexusSlotInformation> type() { return TYPE; }
}
