package net.silvertide.artifactory.client.events;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.keybindings.Keybindings;
import net.silvertide.artifactory.network.server_packets.SB_ToggleManageAttunementsScreen;

@EventBusSubscriber(modid = Artifactory.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void clientTick(ClientTickEvent clientTickEvent) {
        if(Minecraft.getInstance().player == null) return;
        if(Keybindings.INSTANCE.useOpenManageAttunementsKey.consumeClick()) {
            PacketDistributor.sendToServer(new SB_ToggleManageAttunementsScreen());
        }
    }
}
