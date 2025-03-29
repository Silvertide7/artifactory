package net.silvertide.artifactory.network.client_packets;

import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunementDataSource;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record CB_SyncDatapackData(Map<ResourceLocation, AttunementDataSource> dataMap) implements CustomPacketPayload {
    public static final Type<CB_SyncDatapackData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_sync_datapack_data"));
    public static final StreamCodec<FriendlyByteBuf, CB_SyncDatapackData> STREAM_CODEC = StreamCodec.of(
            CB_SyncDatapackData::encode, CB_SyncDatapackData::decode
    );

    public static CB_SyncDatapackData decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, AttunementDataSource> dataMap = new HashMap<>();

        for (int i = 0; i < size; i++) {
            ResourceLocation key = buf.readResourceLocation();
            AttunementDataSource data = AttunementDataSource.CODEC.parse(JsonOps.INSTANCE, net.minecraft.util.GsonHelper.parse(buf.readUtf()))
                    .getOrThrow();
            dataMap.put(key, data);
        }

        return new CB_SyncDatapackData(dataMap);
    }

    public static void encode(FriendlyByteBuf buf, CB_SyncDatapackData packet) {
        // Send how many keys are in the map
        buf.writeVarInt(packet.dataMap().size());
        packet.dataMap().forEach((resourceLocation, itemAttunementData) -> {
            buf.writeResourceLocation(resourceLocation);
            buf.writeUtf(AttunementDataSource.CODEC.encodeStart(JsonOps.INSTANCE, itemAttunementData)
                    .getOrThrow()
                    .toString());
        });
    }

    public static void handle(CB_SyncDatapackData packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientAttunementDataSource.setClientAttunementDataSource(packet.dataMap()));
    }

    @Override
    public @NotNull Type<CB_SyncDatapackData> type() { return TYPE; }
}
