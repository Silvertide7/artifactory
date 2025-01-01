package net.silvertide.artifactory.network.client_packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunedItems;

public record CB_ResetAttunedItems() implements CustomPacketPayload {
    public static final CB_ResetAttunedItems INSTANCE = new CB_ResetAttunedItems();
    public static final Type<CB_ResetAttunedItems> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_reset_attuned_items"));

    //TODO Make sure this is correct.
    public static final StreamCodec<FriendlyByteBuf, CB_ResetAttunedItems> CODEC = StreamCodec.unit(INSTANCE);
    public static void handle(CB_ResetAttunedItems packet, IPayloadContext ctx) {
        ctx.enqueueWork(ClientAttunedItems::clearAllAttunedItems);
    }

    @Override
    public Type<CB_ResetAttunedItems> type() { return TYPE; }
}