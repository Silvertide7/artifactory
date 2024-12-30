package net.silvertide.artifactory.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.codecs.AttunableItems;

@EventBusSubscriber(modid = Artifactory.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class DataGenEvents {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event)
    {
        event.addListener(AttunableItems.DATA_LOADER);
    }
}
