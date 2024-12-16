package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.client.util.ClientUtil;

import java.util.function.Supplier;

public class CB_OpenManageAttunementsScreen {
    private final int numUniqueAttunementsAllowed;
    public CB_OpenManageAttunementsScreen(int numUniqueAttunementsAllowed) {
        this.numUniqueAttunementsAllowed = numUniqueAttunementsAllowed;
    }
    public CB_OpenManageAttunementsScreen(FriendlyByteBuf buf) {
        this(buf.readInt());
    }
    public void encode(FriendlyByteBuf buf) { buf.writeInt(numUniqueAttunementsAllowed); }
    static void handle(CB_OpenManageAttunementsScreen msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientUtil.openManageScreen(msg.numUniqueAttunementsAllowed);
        });
        context.setPacketHandled(true);
    }
}
