package net.silvertide.artifactory.client.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.keybindings.Keybindings;
import net.silvertide.artifactory.network.PacketHandler;
import net.silvertide.artifactory.network.SB_ToggleManageAttunementsScreen;

@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent clientTickEvent) {
        if(Minecraft.getInstance().player == null) return;
        if(Keybindings.INSTANCE.useOpenManageAttunementsKey.consumeClick()) {
            PacketHandler.sendToServer(new SB_ToggleManageAttunementsScreen());
        }
    }
}
