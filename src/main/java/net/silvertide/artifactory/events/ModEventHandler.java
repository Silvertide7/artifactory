package net.silvertide.artifactory.events;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.network.PacketHandler;

@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
    @SubscribeEvent
    public static void commonSetupEvent(FMLCommonSetupEvent commonSetupEvent) {
        commonSetupEvent.enqueueWork(PacketHandler::register);
    }
}
