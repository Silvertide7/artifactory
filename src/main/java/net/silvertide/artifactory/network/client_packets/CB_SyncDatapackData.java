package net.silvertide.artifactory.network.client_packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.codecs.AttunableItems;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record CB_SyncDatapackData(Map<ResourceLocation, AttunementDataSource> dataMap) implements CustomPacketPayload {
    public static final Type<CB_SyncDatapackData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_sync_datapack_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CB_SyncDatapackData> STREAM_CODEC =
            ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceLocation, AttunementDataSource, Map<ResourceLocation, AttunementDataSource>>map(
                            HashMap::new, ResourceLocation.STREAM_CODEC, AttunementDataSource.STREAM_CODEC)
                    .map(CB_SyncDatapackData::new, CB_SyncDatapackData::dataMap);

    public static void handle(CB_SyncDatapackData packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> AttunableItems.setActiveData(packet.dataMap()));
    }

    @Override
    public @NotNull Type<CB_SyncDatapackData> type() { return TYPE; }
}
