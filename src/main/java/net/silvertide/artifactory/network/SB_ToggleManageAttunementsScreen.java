package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.util.PlayerMessenger;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;


public class SB_ToggleManageAttunementsScreen {
    public SB_ToggleManageAttunementsScreen() {}
    public SB_ToggleManageAttunementsScreen(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    static void handle(SB_ToggleManageAttunementsScreen msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleMessage(context.getSender(), msg);
        });
        context.setPacketHandled(true);
    }

    private static void handleMessage(@Nullable ServerPlayer player, SB_ToggleManageAttunementsScreen msg) {
        if(player == null) return;

        if(Config.CAN_USE_KEYBIND_TO_OPEN_MANAGE_SCREEN.get()) {
            PacketHandler.sendToClient(player, new CB_OpenManageAttunementsScreen());
        } else {
            PlayerMessenger.displayTranslatabelClientMessage(player, "playermessage.artifactory.cant_use_manage_keybind");
        }
    }
}
