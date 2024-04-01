package net.silvertide.artifactory.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.codecs.AttuneableItems;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.util.ArtifactUtil;
import net.silvertide.artifactory.util.CapabilityUtil;
import net.silvertide.artifactory.util.PlayerMessenger;


@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArtifactEvents {

    @SubscribeEvent(priority= EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled() || event.getSource().getEntity() == null) return;

        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide()) {
            LivingEntity target = event.getEntity();
            if (target == null) return;

            ItemStack stackInHand = player.getMainHandItem();
            ArtifactUtil.getAttunementData(stackInHand).ifPresent(itemAttunementData -> {
                if(!itemAttunementData.useWithoutAttunement() && !ArtifactUtil.arePlayerAndItemAttuned(player, stackInHand)) {
                    event.setCanceled(true);
                    PlayerMessenger.displayClientMessage(player, "Must attune to item to attack.");
                }
            });
        }
    }

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

            ArtifactUtil.getAttunementData(stack).ifPresent(itemAttunementData -> Artifactory.LOGGER.info("Item Attunemeent Data: \n" + itemAttunementData));
            Artifactory.LOGGER.info("Item thrown attuned to player: " + ArtifactUtil.arePlayerAndItemAttuned(player, stack));

            Artifactory.LOGGER.info("Item NBT: " + stack.getOrCreateTag());
        }

    }
}
