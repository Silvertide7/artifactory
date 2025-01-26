package net.silvertide.artifactory.network.server_packets;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.gui.AttunementMenu;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.util.AttunementService;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SB_RemoveAttunedItem(UUID itemUUIDToRemove) implements CustomPacketPayload {
    public static final Type<SB_RemoveAttunedItem> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "sb_remove_attuned_item"));
    public static final StreamCodec<FriendlyByteBuf, SB_RemoveAttunedItem> STREAM_CODEC = StreamCodec
            .composite(UUIDUtil.STREAM_CODEC, SB_RemoveAttunedItem::itemUUIDToRemove,
                    SB_RemoveAttunedItem::new);

    public static void handle(SB_RemoveAttunedItem packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if(ctx.player() instanceof ServerPlayer player) {
                ArtifactorySavedData.get().removeAttunedItem(player.getUUID(), packet.itemUUIDToRemove);
                AttunementService.clearBrokenAttunements(player);
                if(player.containerMenu instanceof AttunementMenu attuneMenu && player.containerMenu.stillValid(player)) {
                    attuneMenu.updateAttunementItemDataComponent();
                }
            }
        });
    }

    @Override
    public @NotNull Type<SB_RemoveAttunedItem> type() { return TYPE; }
}
