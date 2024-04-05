package net.silvertide.artifactory.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.util.ArtifactUtil;
import net.silvertide.artifactory.util.PlayerMessenger;
import net.silvertide.artifactory.util.StackNBTUtil;

import java.util.List;


@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArtifactEvents {

    @SubscribeEvent(priority= EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled() || event.getSource().getEntity() == null) return;

        if (event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            if (target == null) return;

            ItemStack stackInHand = player.getMainHandItem();
            if(ArtifactUtil.isUseRestricted(player, stackInHand)) {
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
        if(ArtifactUtil.isUseRestricted(player, stack)){
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
        if(ArtifactUtil.isUseRestricted(player, stack)){
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
            ArtifactorySavedData artifactorySavedData = ArtifactorySavedData.get();
            artifactorySavedData.getAttunedItemsAsList(player.getUUID()).ifPresent(attunedItemsList -> {
                for(int i = 0; i < attunedItemsList.size(); i++) {
                    Artifactory.LOGGER.info("Attuned item " + i + ": " + attunedItemsList.get(i));
                }
            });

            ArtifactUtil.getAttunementData(stack).ifPresent(itemAttunementData -> Artifactory.LOGGER.info("Item Attunemeent Data: \n" + itemAttunementData));
            Artifactory.LOGGER.info("Item thrown attuned to player: " + ArtifactUtil.arePlayerAndItemAttuned(player, stack));

            Artifactory.LOGGER.info("Item NBT: " + stack.getOrCreateTag());
        }
    }

    // This implementation of checking the players items and giving negative effects based on the attunement requirements
    // was adapted from Project MMO
    //https://github.com/Caltinor/Project-MMO-2.0/blob/main/src/main/java/harmonised/pmmo/events/impl/PlayerTickHandler.java
    private static short ticksIgnoredSinceLastProcess = 0;
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent playerTickEvent) {
        ticksIgnoredSinceLastProcess++;
        if (playerTickEvent.phase == TickEvent.Phase.END || ticksIgnoredSinceLastProcess < 10) return;
        Player player = playerTickEvent.player;

        if (player instanceof ServerPlayer) {
            Inventory inv = player.getInventory();

            List<ItemStack> items = List.of(inv.getItem(36), inv.getItem(37), inv.getItem(38), inv.getItem(39));

            for (ItemStack stack : items) {
                if (!stack.isEmpty() && ArtifactUtil.isUseRestricted(player, stack)) {
                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation("minecraft:slowness"));
                    if(effect != null){
                        MobEffectInstance mobEffectInstance = new MobEffectInstance(effect, 15, 3);
                        if (!player.hasEffect(mobEffectInstance.getEffect()) || player.getEffect(mobEffectInstance.getEffect()).getDuration() < 10) {
                            player.addEffect(mobEffectInstance);
                        }
                    }
                }
            }
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
