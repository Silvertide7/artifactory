package net.silvertide.artifactory.setup;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.gui.AttunementScreen;
import net.silvertide.artifactory.registry.MenuRegistry;

@EventBusSubscriber(modid = Artifactory.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerMenuScreen(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.ATTUNEMENT_NEXUS_MENU.get(), AttunementScreen::new);
    }
}
