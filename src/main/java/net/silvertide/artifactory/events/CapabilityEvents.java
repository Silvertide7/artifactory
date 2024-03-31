package net.silvertide.artifactory.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.capabilities.AttunedItems;
import net.silvertide.artifactory.capabilities.AttunedItemsAttacher;
import net.silvertide.artifactory.util.CapabilityUtil;

@Mod.EventBusSubscriber(modid= Artifactory.MOD_ID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(AttunedItems.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            AttunedItemsAttacher.attach(event);
        }
    }

    @SubscribeEvent(priority= EventPriority.LOWEST)
    public static void playerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        oldPlayer.revive();
        Player newPlayer = event.getEntity();

        CapabilityUtil.getAttunedItems(oldPlayer).ifPresent(oldAttunedItems -> CapabilityUtil.getAttunedItems(newPlayer).ifPresent(newAttunedItems -> {
            newAttunedItems.setAttunedItems(oldAttunedItems.getAttunedItemsMap());
        }));
        oldPlayer.invalidateCaps();
    }

}
