package net.silvertide.artifactory.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.gui.AttunementNexusManageScreen;

import java.util.function.Supplier;

public class CB_OpenManageAttunementsScreen {
    public CB_OpenManageAttunementsScreen() {}
    public CB_OpenManageAttunementsScreen(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
    static void handle(CB_OpenManageAttunementsScreen msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if(!(minecraft.screen instanceof AttunementNexusManageScreen)) {
                minecraft.setScreen(new AttunementNexusManageScreen());
            }
        });
        context.setPacketHandled(true);
    }
}
