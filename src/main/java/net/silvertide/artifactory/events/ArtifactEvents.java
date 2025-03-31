package net.silvertide.artifactory.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientAttunementUtil;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.services.AttunementService;
import net.silvertide.artifactory.util.*;

import java.util.List;


@EventBusSubscriber(modid = Artifactory.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ArtifactEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(AttackEntityEvent event) {
        if (event.isCanceled()) return;
        if (event.getEntity() instanceof Player player) {
            List<ItemStack> itemsInHand = List.of(player.getMainHandItem(), player.getOffhandItem());
            for(ItemStack stack : itemsInHand) {
                if(AttunementUtil.isUseRestricted(player, stack)) {
                    event.setCanceled(true);
                    break;
                }
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if(event.isCanceled()) return;

        ItemStack stack = event.getItemStack();

        if(sidedIsUseRestricted(player, stack)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if(event.isCanceled()) return;

        ItemStack stack = event.getItemStack();
        if(sidedIsUseRestricted(player, stack)) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    private static boolean sidedIsUseRestricted(Player player, ItemStack stack) {
        if(player instanceof ServerPlayer) {
            return AttunementUtil.isUseRestricted(player, stack);
        } else {
            return ClientAttunementUtil.isUseRestricted(player, stack);
        }
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre itemPickupEvent) {
        if(itemPickupEvent.getPlayer() instanceof ServerPlayer serverPlayer) {
            ItemStack stack = itemPickupEvent.getItemEntity().getItem();
            AttunementService.checkAndUpdateAttunementComponents(stack);
            if(AttunementUtil.isValidAttunementItem(stack)
                    && AttunementUtil.isAttunedToAnotherPlayer(serverPlayer, stack)) {
                itemPickupEvent.setCanPickup(TriState.FALSE);
            }
        }
    }

//    @SubscribeEvent
//    public static void throwItem(ItemTossEvent itemTossEvent) {
//        if(itemTossEvent.getPlayer() instanceof ServerPlayer serverPlayer) {
//            ArtifactorySavedData.get().logAttunedItems(serverPlayer);
//        } else {
//            Artifactory.LOGGER.info("Client Items:");
//            for(AttunedItem attunedItem : ClientAttunedItems.getAttunedItemsAsList()) {
//                Artifactory.LOGGER.info(attunedItem.toString());
//            }
//        }
//    }

    // This implementation of checking the players items and giving negative effects based on the attunement requirements
    // was adapted from Project MMO
    // https://github.com/Caltinor/Project-MMO-2.0/blob/main/src/main/java/harmonised/pmmo/events/impl/PlayerTickHandler.java
    private static short ticksIgnoredSinceLastProcess = 0;
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post playerTickEvent) {
        if (playerTickEvent.getEntity() instanceof ServerPlayer serverPlayer) {
            ticksIgnoredSinceLastProcess++;
            if (ticksIgnoredSinceLastProcess < 18) return;
            ticksIgnoredSinceLastProcess = 0;

            Inventory inv = serverPlayer.getInventory();
            List<ItemStack> armorItems = List.of(inv.getItem(36), inv.getItem(37), inv.getItem(38), inv.getItem(39));

            for (ItemStack armorStack : armorItems) {
                if(armorStack.isEmpty()) continue;
                AttunementService.checkAndUpdateAttunementComponents(armorStack);

                AttunementService.applyEffectsToPlayer(serverPlayer, armorStack, true);
            }

            List<ItemStack> handItems= List.of(serverPlayer.getMainHandItem(), serverPlayer.getOffhandItem());
            for(ItemStack handStack : handItems) {
                if(handStack.isEmpty()) continue;
                AttunementService.checkAndUpdateAttunementComponents(handStack);

                AttunementService.applyEffectsToPlayer(serverPlayer, handStack, false);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent entityJoinLevelEvent) {
        if(entityJoinLevelEvent.getEntity() instanceof ItemEntity itemEntity && !entityJoinLevelEvent.getLevel().isClientSide()) {
            ItemStack stack = itemEntity.getItem();
            if(AttunementUtil.isValidAttunementItem(stack) && AttunementUtil.isItemAttunedToAPlayer(stack)) {
                itemEntity.setUnlimitedLifetime();
                if(DataComponentUtil.getPlayerAttunementData(stack).map(PlayerAttunementData::isInvulnerable).orElse(false)) {
                    itemEntity.setInvulnerable(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemEntityExpire(ItemExpireEvent itemExpireEvent) {
        ItemStack stack = itemExpireEvent.getEntity().getItem();
        if(!stack.isEmpty() && AttunementUtil.isItemAttunedToAPlayer(stack)) {
            AttunementService.removeAttunementFromPlayerAndItem(stack);
        }
    }

    @SubscribeEvent
    public static void onPlayerDestroyItemEvent(PlayerDestroyItemEvent playerDestroyItemEvent) {
        ItemStack stack = playerDestroyItemEvent.getOriginal();
        if(!stack.isEmpty() && AttunementUtil.isItemAttunedToAPlayer(stack)) {
            AttunementService.removeAttunementFromPlayerAndItem(stack);
        }
    }

    @SubscribeEvent
    public static void onApplyAttributeModifier(ItemAttributeModifierEvent attributeModifierEvent) {
        // Check the artifactory attributes data and apply attribute modifiers
        ItemStack stack = attributeModifierEvent.getItemStack();

        boolean isValidAttunementItem = switch(FMLEnvironment.dist) {
            case CLIENT -> ClientAttunementUtil.isValidAttunementItem(stack);
            case DEDICATED_SERVER -> AttunementUtil.isValidAttunementItem(stack);
        };

        if(isValidAttunementItem) {
            DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
                attunementData.attributeModifications().forEach(modification -> modification.addAttributeModifier(attributeModifierEvent));
            });
        }
    }
}
