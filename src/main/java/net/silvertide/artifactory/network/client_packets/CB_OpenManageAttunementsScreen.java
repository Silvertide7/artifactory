package net.silvertide.artifactory.network.client_packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.util.ClientUtil;
import org.jetbrains.annotations.NotNull;

public record CB_OpenManageAttunementsScreen() implements CustomPacketPayload {
    public static final CB_OpenManageAttunementsScreen INSTANCE = new CB_OpenManageAttunementsScreen();
    public static final Type<CB_OpenManageAttunementsScreen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "cb_open_manage_screen"));
    public static final StreamCodec<FriendlyByteBuf, CB_OpenManageAttunementsScreen> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(CB_OpenManageAttunementsScreen packet, IPayloadContext ctx) {
        ctx.enqueueWork(ClientUtil::openManageScreen);
    }
    @Override
    public @NotNull Type<CB_OpenManageAttunementsScreen> type() { return TYPE; }
}
