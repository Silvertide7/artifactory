package net.silvertide.artifactory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.silvertide.artifactory.gui.AttunementMenu;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.util.AttunementService;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class SB_RemoveAttunedItem {
    private final UUID itemUUIDToRemove;
    public SB_RemoveAttunedItem(UUID itemUUIDToRemove) {
        this.itemUUIDToRemove = itemUUIDToRemove;
    }
    public SB_RemoveAttunedItem(FriendlyByteBuf buf) {
        itemUUIDToRemove = buf.readUUID();
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(itemUUIDToRemove);
    }

    static void handle(SB_RemoveAttunedItem msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleMessage(context.getSender(), msg);
        });
        context.setPacketHandled(true);
    }

    private static void handleMessage(@Nullable ServerPlayer player, SB_RemoveAttunedItem msg) {
        if(player != null) {
            ArtifactorySavedData.get().removeAttunedItem(player.getUUID(), msg.itemUUIDToRemove);
            AttunementService.clearBrokenAttunements(player);
            if(player.containerMenu instanceof AttunementMenu attuneMenu && player.containerMenu.stillValid(player)) {
                attuneMenu.updateAttunementItemNBT();
            }
        }
    }
}
