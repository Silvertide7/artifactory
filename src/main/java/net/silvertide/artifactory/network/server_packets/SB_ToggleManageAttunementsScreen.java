package net.silvertide.artifactory.network.server_packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.network.client_packets.CB_OpenManageAttunementsScreen;
import net.silvertide.artifactory.services.PlayerMessenger;
import org.jetbrains.annotations.NotNull;

public record SB_ToggleManageAttunementsScreen() implements CustomPacketPayload {
    public static final SB_ToggleManageAttunementsScreen INSTANCE = new SB_ToggleManageAttunementsScreen();
    public static final Type<SB_ToggleManageAttunementsScreen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "sb_toggle_manage_screen"));
    //TODO Make sure this is correct.
    public static final StreamCodec<FriendlyByteBuf, SB_ToggleManageAttunementsScreen> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static void handle(SB_ToggleManageAttunementsScreen ignoredPacket, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if(ctx.player() instanceof ServerPlayer player) {
                if(ServerConfigs.CAN_USE_KEYBIND_TO_OPEN_MANAGE_SCREEN.get()) {
                    PacketDistributor.sendToPlayer(player, new CB_OpenManageAttunementsScreen(ServerConfigs.NUMBER_UNIQUE_ATTUNEMENTS_PER_PLAYER.get()));
                } else {
                    PlayerMessenger.displayTranslatabelClientMessage(player, "playermessage.artifactory.cant_use_manage_keybind");
                }
            }
        });
    }
    @Override
    public @NotNull Type<SB_ToggleManageAttunementsScreen> type() { return TYPE; }
}
