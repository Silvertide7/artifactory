package net.silvertide.artifactory.events;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.ArtifactUtil;
import net.silvertide.artifactory.util.CapabilityUtil;
import net.silvertide.artifactory.util.PlayerMessenger;
import net.silvertide.artifactory.util.StackNBTUtil;


@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArtifactEvents {

    @SubscribeEvent(priority= EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled() || event.getSource().getEntity() == null) return;

        if (event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            if (target == null) return;

            ItemStack stackInHand = player.getMainHandItem();
            if(!ArtifactUtil.isItemUseable(player, stackInHand)) {
                event.setCanceled(true);
                PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.item_not_usable");
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if(event.isCanceled()) return;

        ItemStack stack = event.getItemStack();
        if(!ArtifactUtil.isItemUseable(player, stack)){
            event.setUseItem(Event.Result.DENY);
            event.setUseBlock(Event.Result.DENY);
            event.setCanceled(true);
            PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.item_not_usable");
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if(event.isCanceled()) return;

        //TODO: mayNeed to sync a copy of the attunement data of the player to the client so it can check if the item
        // is attuned to the player to have the player see what is really happening. Only sending messages from the
        // server side seems to fix this though.
        ItemStack stack = event.getItemStack();
        if(!ArtifactUtil.isItemUseable(player, stack)){
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            if(!player.level().isClientSide()) PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.item_not_usable");
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

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent entityJoinLevelEvent) {
        if(!entityJoinLevelEvent.getLevel().isClientSide() && entityJoinLevelEvent.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if(ArtifactUtil.isItemAttuned(stack)) {
                Artifactory.LOGGER.info("Found attuned item to make invulnerable");
                itemEntity.setUnlimitedLifetime();
                if(StackNBTUtil.isInvulnerable(stack)) {
                    Artifactory.LOGGER.info("Making invulnerable");
                    itemEntity.setInvulnerable(true);
                }
            }
        }
    }
}
