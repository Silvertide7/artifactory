package net.silvertide.artifactory.network.client_packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunedItems;
import net.silvertide.artifactory.storage.AttunedItem;
import org.jetbrains.annotations.NotNull;

public record CB_UpdateAttunedItem(AttunedItem attunedItem) implements CustomPacketPayload {
    public static final Type<CB_UpdateAttunedItem> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_update_attuned_item"));
    public static final StreamCodec<FriendlyByteBuf, CB_UpdateAttunedItem> STREAM_CODEC = StreamCodec
            .composite(AttunedItem.STREAM_CODEC, CB_UpdateAttunedItem::attunedItem, CB_UpdateAttunedItem::new);

    public static void handle(CB_UpdateAttunedItem packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientAttunedItems.setAttunedItem(packet.attunedItem));
    }
    @Override
    public @NotNull Type<CB_UpdateAttunedItem> type() { return TYPE; }
}
