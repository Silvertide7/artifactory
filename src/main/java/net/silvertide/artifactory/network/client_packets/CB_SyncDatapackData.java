package net.silvertide.artifactory.network.client_packets;

//TODO: Implement this in the sync event
public class CB_SyncDatapackData {
//    private final Map<ResourceLocation, AttunementDataSource> dataMap;
//
//    public CB_SyncDatapackData(Map<ResourceLocation, AttunementDataSource> dataMap) {
//        this.dataMap = dataMap;
//    }
//    public static CB_SyncDatapackData decode(FriendlyByteBuf buf) {
//        int size = buf.readVarInt();
//        Map<ResourceLocation, AttunementDataSource> dataMap = new HashMap<>();
//
//        for (int i = 0; i < size; i++) {
//            ResourceLocation key = buf.readResourceLocation();
//            String jsonData = buf.readUtf();
//            AttunementDataSource data = AttunementDataSource.CODEC.parse(JsonOps.INSTANCE, net.minecraft.util.GsonHelper.parse(jsonData))
//                    .getOrThrow(true, error -> Artifactory.LOGGER.error("CB_UpdateAttunementDatat - Failed to decode: " + error));
//            dataMap.put(key, data);
//        }
//
//        return new CB_SyncDatapackData(dataMap);
//    }
//
//    public void encode(FriendlyByteBuf buf) {
//        // Send how many keys are in the map
//        buf.writeVarInt(dataMap.size());
//        dataMap.forEach((resourceLocation, itemAttunementData) -> {
//            buf.writeResourceLocation(resourceLocation);
//            buf.writeUtf(AttunementDataSource.CODEC.encodeStart(JsonOps.INSTANCE, itemAttunementData)
//                    .getOrThrow(true, error -> Artifactory.LOGGER.error("CB_UpdateAttunementDatat - Failed to encode: " + error))
//                    .toString());
//        });
//    }
//
//    static void handle(CB_SyncDatapackData msg, Supplier<NetworkEvent.Context> contextSupplier) {
//        NetworkEvent.Context context = contextSupplier.get();
//        context.enqueueWork(() -> {
//            ClientItemAttunementData.setAttunementData(msg.dataMap);
//        });
//        context.setPacketHandled(true);
//    }
}
