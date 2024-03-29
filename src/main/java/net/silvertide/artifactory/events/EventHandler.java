package net.silvertide.artifactory.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.codecs.AttuneableItems;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent tossEvent) {
        Player player = tossEvent.getPlayer();
        if(player.level().isClientSide() || tossEvent.isCanceled()) return;

        Map<ResourceLocation, ItemAttunementData> itemAttunementDataMap = AttuneableItems.DATA_LOADER.getData();

        ItemStack stack = tossEvent.getEntity().getItem();

        if(!stack.isEmpty()) {
//            ArtifactUtil.sendSystemMessage(player, "Item is an artifact:");
            ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if(itemAttunementDataMap.containsKey(resourceLocation)) {
                Artifactory.LOGGER.info("Item thrown: " + resourceLocation);
                ItemAttunementData attunementData = itemAttunementDataMap.get(resourceLocation);
                Artifactory.LOGGER.info("Item is attuneable: \n" + attunementData.toString());
            }
        }

    }
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event)
    {
        event.addListener(AttuneableItems.DATA_LOADER);
    }
}
