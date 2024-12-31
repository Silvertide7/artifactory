package net.silvertide.artifactory.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.silvertide.artifactory.Artifactory;

@EventBusSubscriber(modid = Artifactory.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void commonSetupEvent(FMLCommonSetupEvent commonSetupEvent) {
        commonSetupEvent.enqueueWork(PacketHandler::register);
    }
}
