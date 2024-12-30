package net.silvertide.artifactory.network.client_packets;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.util.ClientUtil;

import java.util.UUID;

public record CB_RemoveAttunedItem(UUID itemUUIDToRemove) implements CustomPacketPayload {
    public static final Type<CB_RemoveAttunedItem> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_remove_attuned_item"));
    public static final StreamCodec<FriendlyByteBuf, CB_RemoveAttunedItem> CODEC = StreamCodec
            .composite(UUIDUtil.STREAM_CODEC, CB_RemoveAttunedItem::itemUUIDToRemove,
                    CB_RemoveAttunedItem::new);

    public static void handle(CB_RemoveAttunedItem packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientUtil.removeAttunedItem(packet.itemUUIDToRemove);
        });
    }

    @Override
    public Type<CB_RemoveAttunedItem> type() { return TYPE; }
}
