package net.silvertide.artifactory.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.network.CB_UpdateAttunedItemMessage;
import net.silvertide.artifactory.network.PacketHandler;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.Map;
import java.util.UUID;


@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SystemEvents {
    @SubscribeEvent(priority= EventPriority.LOW)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            //===========UPDATE DATA MIRROR=======================
            Map<UUID, AttunedItem> attunedItems = ArtifactorySavedData.get().getAttunedItems(player.getUUID());
            for(Map.Entry<UUID, AttunedItem> playersAttunedItems : attunedItems.entrySet()) {
                PacketHandler.sendToClient((ServerPlayer) player, new CB_UpdateAttunedItemMessage(playersAttunedItems.getValue()));
            }
        }
    }
}
