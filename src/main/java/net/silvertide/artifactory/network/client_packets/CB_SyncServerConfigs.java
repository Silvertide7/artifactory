package net.silvertide.artifactory.network.client_packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunedItems;
import net.silvertide.artifactory.client.state.ClientSyncedConfig;
import org.jetbrains.annotations.NotNull;

public record CB_SyncServerConfigs(Integer xpThreshold, Integer xpConsumed) implements CustomPacketPayload {
    public static final Type<CB_SyncServerConfigs> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_sync_server_configs"));
    public static final StreamCodec<FriendlyByteBuf, CB_SyncServerConfigs> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.INT, CB_SyncServerConfigs::xpThreshold,
                    ByteBufCodecs.INT, CB_SyncServerConfigs::xpConsumed,
                    CB_SyncServerConfigs::new
    );

    public static void handle(CB_SyncServerConfigs packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientSyncedConfig.updateConfigs(packet.xpThreshold, packet.xpConsumed);
        });
    }

    @Override
    public @NotNull Type<CB_SyncServerConfigs> type() { return TYPE; }
}
