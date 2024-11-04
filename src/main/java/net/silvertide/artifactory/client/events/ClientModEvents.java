package net.silvertide.artifactory.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.keybindings.Keybindings;

@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent keyMappingsEvent) {
        keyMappingsEvent.register(Keybindings.INSTANCE.useOpenManageAttunementsKey);
    }
}

