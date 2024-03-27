package net.silvertide.artifactory.registry;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.capabilities.AttunedItems;

@Mod.EventBusSubscriber(modid= Artifactory.MOD_ID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityRegistry {
    public static final Capability<AttunedItems> ATTUNED_ITEMS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(AttunedItems.class);
    }

}
