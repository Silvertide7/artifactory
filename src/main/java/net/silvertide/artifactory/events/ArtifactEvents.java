package net.silvertide.artifactory.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.ArtifactUtil;
import net.silvertide.artifactory.util.CapabilityUtil;


@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArtifactEvents {

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent tossEvent) {
        Player player = tossEvent.getPlayer();
        if(player.level().isClientSide() || tossEvent.isCanceled()) return;
        ItemStack stack = tossEvent.getEntity().getItem();

        if(!stack.isEmpty()) {
            CapabilityUtil.getAttunedItems(player).ifPresent(attunedItems -> {
                attunedItems.getAttunedItemsAsList().ifPresent(items -> {
                    for(int i = 0; i < items.size(); i++) {
                        Artifactory.LOGGER.info("Attuned item " + i + ": " + items.get(i));
                    }
                });
            });
            Artifactory.LOGGER.info("Item thrown attuned to player: " + ArtifactUtil.arePlayerAndStackAttuned(player, stack));
            Artifactory.LOGGER.info("Item NBT: " + stack.getOrCreateTag());
        }

    }
}
