package net.silvertide.artifactory.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.gui.AttunementScreen;
import net.silvertide.artifactory.gui.ManageAttunementsScreen;

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
            Minecraft minecraft = Minecraft.getInstance();

            if(minecraft.screen instanceof AttunementScreen) {
                minecraft.pushGuiLayer(new ManageAttunementsScreen(msg.numUniqueAttunementsAllowed));
            }else if(!(minecraft.screen instanceof ManageAttunementsScreen)) {
                minecraft.setScreen(new ManageAttunementsScreen(msg.numUniqueAttunementsAllowed));
            }
        });
        context.setPacketHandled(true);
    }
}
