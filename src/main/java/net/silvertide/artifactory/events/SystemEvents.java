package net.silvertide.artifactory.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.commands.CmdRoot;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.NetworkUtil;

import java.util.Map;
import java.util.UUID;


@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SystemEvents {
    @SubscribeEvent(priority= EventPriority.LOW)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            //===========UPDATE DATA MIRROR=======================
            Map<UUID, AttunedItem> attunedItems = ArtifactorySavedData.get().getAttunedItems(serverPlayer.getUUID());
            NetworkUtil.updateAllAttunedItems(serverPlayer, attunedItems);
            ArtifactorySavedData.get().updatePlayerDisplayName(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CmdRoot.register(event.getDispatcher());
    }
}
