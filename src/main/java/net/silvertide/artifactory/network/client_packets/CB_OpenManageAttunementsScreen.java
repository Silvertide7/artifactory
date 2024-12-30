package net.silvertide.artifactory.network.client_packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.util.ClientUtil;

public record CB_OpenManageAttunementsScreen(int numUniqueAttunementsAllowed) implements CustomPacketPayload {
    public static final Type<CB_OpenManageAttunementsScreen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_open_manage_screen"));
    public static final StreamCodec<FriendlyByteBuf, CB_OpenManageAttunementsScreen> CODEC = StreamCodec
            .composite(ByteBufCodecs.INT, CB_OpenManageAttunementsScreen::numUniqueAttunementsAllowed,
                    CB_OpenManageAttunementsScreen::new);

    public static void handle(CB_OpenManageAttunementsScreen packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientUtil.openManageScreen(packet.numUniqueAttunementsAllowed);
        });
    }
    @Override
    public Type<CB_OpenManageAttunementsScreen> type() { return TYPE; }
}
