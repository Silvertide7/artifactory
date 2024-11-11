package net.silvertide.artifactory.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.client.state.ClientAttunedItems;
import net.silvertide.artifactory.gui.ManageAttunementsScreen;

import java.util.UUID;
import java.util.function.Supplier;


public class CB_RemoveAttunedItem {
    private final UUID itemUUIDToRemove;
    public CB_RemoveAttunedItem(UUID itemUUIDToRemove) {
        this.itemUUIDToRemove = itemUUIDToRemove;
    }
    public CB_RemoveAttunedItem(FriendlyByteBuf buf) {
        itemUUIDToRemove = buf.readUUID();
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(itemUUIDToRemove);
    }

    static void handle(CB_RemoveAttunedItem msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientAttunedItems.removeAttunedItem(msg.itemUUIDToRemove);

            // Update screen of minecraft player if open
            Minecraft minecraft = Minecraft.getInstance();
            if(minecraft.screen instanceof ManageAttunementsScreen manageAttunementsScreen) {
                manageAttunementsScreen.createAttunementCards();
            }
        });
        context.setPacketHandled(true);
    }
}
