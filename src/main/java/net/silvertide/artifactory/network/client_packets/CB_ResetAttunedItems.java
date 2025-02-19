package net.silvertide.artifactory.network.client_packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunedItems;
import org.jetbrains.annotations.NotNull;

public record CB_ResetAttunedItems() implements CustomPacketPayload {
    public static final CB_ResetAttunedItems INSTANCE = new CB_ResetAttunedItems();
    public static final Type<CB_ResetAttunedItems> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_reset_attuned_items"));
    public static final StreamCodec<FriendlyByteBuf, CB_ResetAttunedItems> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(CB_ResetAttunedItems ignoredPacket, IPayloadContext ctx) {
        ctx.enqueueWork(ClientAttunedItems::clearAllAttunedItems);
    }

    @Override
    public @NotNull Type<CB_ResetAttunedItems> type() { return TYPE; }
}
