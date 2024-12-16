package net.silvertide.artifactory.network;

import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientItemAttunementData;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class CB_UpdateAttunementData {
    private final Map<ResourceLocation, ItemAttunementData> dataMap;

    public CB_UpdateAttunementData(Map<ResourceLocation, ItemAttunementData> dataMap) {
        this.dataMap = dataMap;
    }
    public static CB_UpdateAttunementData decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, ItemAttunementData> dataMap = new HashMap<>();

        for (int i = 0; i < size; i++) {
            ResourceLocation key = buf.readResourceLocation();
            String jsonData = buf.readUtf();
            ItemAttunementData data = ItemAttunementData.CODEC.parse(JsonOps.INSTANCE, net.minecraft.util.GsonHelper.parse(jsonData))
                    .getOrThrow(true, error -> Artifactory.LOGGER.error("CB_UpdateAttunementDatat - Failed to decode: " + error));
            dataMap.put(key, data);
        }

        return new CB_UpdateAttunementData(dataMap);
    }

    public void encode(FriendlyByteBuf buf) {
        // Send how many keys are in the map
        buf.writeVarInt(dataMap.size());
        dataMap.forEach((resourceLocation, itemAttunementData) -> {
            buf.writeResourceLocation(resourceLocation);
            buf.writeUtf(ItemAttunementData.CODEC.encodeStart(JsonOps.INSTANCE, itemAttunementData)
                    .getOrThrow(true, error -> Artifactory.LOGGER.error("CB_UpdateAttunementDatat - Failed to encode: " + error))
                    .toString());
        });
    }

    static void handle(CB_UpdateAttunementData msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientItemAttunementData.setAttunementData(msg.dataMap);
        });
        context.setPacketHandled(true);
    }
}
