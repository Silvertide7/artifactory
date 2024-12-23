package net.silvertide.artifactory.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientItemAttunementData;
import net.silvertide.artifactory.modifications.AttributeModification;
import net.silvertide.artifactory.util.*;

import java.util.List;


@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArtifactEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled() || event.getSource().getEntity() == null) return;

        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide()) {
            LivingEntity target = event.getEntity();
            if (target == null) return;

            List<ItemStack> itemsInHand = List.of(player.getMainHandItem(), player.getOffhandItem());
            for(ItemStack stack : itemsInHand) {
                if(sidedIsUseRestricted(player, stack)) {
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

        if(sidedIsUseRestricted(player, stack)){
            event.setUseItem(Event.Result.DENY);
            event.setUseBlock(Event.Result.DENY);
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
        if(EffectiveSide.get().isClient()) {
            return ClientItemAttunementData.isUseRestricted(player, stack);
        } else {
            return AttunementUtil.isUseRestricted(player, stack);
        }
    }

    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent itemPickupEvent) {
        Player player = itemPickupEvent.getEntity();
        if(player.level().isClientSide() || itemPickupEvent.isCanceled()) return;
        AttunementService.clearBrokenAttunementIfExists(itemPickupEvent.getStack());
    }

    // This implementation of checking the players items and giving negative effects based on the attunement requirements
    // was adapted from Project MMO
    // https://github.com/Caltinor/Project-MMO-2.0/blob/main/src/main/java/harmonised/pmmo/events/impl/PlayerTickHandler.java
    private static short ticksIgnoredSinceLastProcess = 0;
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent playerTickEvent) {
        ticksIgnoredSinceLastProcess++;
        if (playerTickEvent.phase == TickEvent.Phase.END || ticksIgnoredSinceLastProcess < 18) return;
        ticksIgnoredSinceLastProcess = 0;

        Player player = playerTickEvent.player;

        if (player instanceof ServerPlayer) {
            Inventory inv = player.getInventory();
            List<ItemStack> armorItems = List.of(inv.getItem(36), inv.getItem(37), inv.getItem(38), inv.getItem(39));

            for (ItemStack armorStack : armorItems) {
                if(armorStack.isEmpty()) continue;
                AttunementService.clearBrokenAttunementIfExists(armorStack);

                AttunementService.applyEffectsToPlayer(player, armorStack, true);
            }

            List<ItemStack> handItems= List.of(player.getMainHandItem(), player.getOffhandItem());
            for(ItemStack handStack : handItems) {
                if(handStack.isEmpty()) continue;
                AttunementService.clearBrokenAttunementIfExists(handStack);

                AttunementService.applyEffectsToPlayer(player, handStack, false);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent entityJoinLevelEvent) {
        if(!entityJoinLevelEvent.getLevel().isClientSide() && entityJoinLevelEvent.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if(AttunementUtil.isValidAttunementItem(stack) && AttunementUtil.isItemAttunedToAPlayer(stack)) {
                itemEntity.setUnlimitedLifetime();
                if(StackNBTUtil.isInvulnerable(stack)) {
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

        boolean isValidAttunementItem;
        if(EffectiveSide.get().isClient()) {
            isValidAttunementItem = ClientItemAttunementData.isValidAttunementItem(stack);
        } else {
            isValidAttunementItem = AttunementUtil.isValidAttunementItem(stack);
        }

        if (isValidAttunementItem && StackNBTUtil.containsAttributeModifications(stack)) {
            CompoundTag artifactoryAttributeModificationsTag = StackNBTUtil.getOrCreateAttributeModificationNBT(stack);
            for(String attributeModificationKey : artifactoryAttributeModificationsTag.getAllKeys()) {
                AttributeModification.fromCompoundTag(artifactoryAttributeModificationsTag.getCompound(attributeModificationKey)).ifPresent(attributeModification -> {
                    if(attributeModification.getEquipmentSlot() == attributeModifierEvent.getSlotType()){
                        attributeModification.addAttributeModifier(attributeModifierEvent);
                    }
                });
            }
        }
    }
}
