package net.silvertide.artifactory.network.client_packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunedItems;

public record CB_UpdateAttunedItemModifications(String attunedItemModifications) implements CustomPacketPayload {
    public static final Type<CB_UpdateAttunedItemModifications> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_remove_attuned_item"));
    public static final StreamCodec<FriendlyByteBuf, CB_UpdateAttunedItemModifications> CODEC = StreamCodec
            .composite(ByteBufCodecs.STRING_UTF8, CB_UpdateAttunedItemModifications::attunedItemModifications,
                    CB_UpdateAttunedItemModifications::new);

    public static void handle(CB_UpdateAttunedItemModifications packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            String[] modificationInformation = packet.attunedItemModifications.split(";");
            if(modificationInformation.length == 2) {
                String resourceLocation = modificationInformation[0];
                String modificationDescription = modificationInformation[1];
                ClientAttunedItems.setModification(resourceLocation, modificationDescription);
            }
        });
    }

    @Override
    public Type<CB_UpdateAttunedItemModifications> type() { return TYPE; }
}
