package net.silvertide.artifactory.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.StackNBTUtil;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid= Artifactory.MOD_ID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class SoulboundEvents {
    public static final String ARTIFACTORY_INV_TAG = "artifactoryInventory";

    // Most of the implementation for keeping items in the inventory was taken from Twilight Forests charm of keeping.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeath(LivingDeathEvent livingDeathEvent) {
        LivingEntity livingEntity = livingDeathEvent.getEntity();
        if (livingDeathEvent.getEntity().level().isClientSide() || livingDeathEvent.isCanceled() || !(livingEntity instanceof Player player) || livingEntity instanceof FakePlayer ||
                player.isCreative() || player.isSpectator()) return;

        if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            keepSoulboundItems(player);
        }
    }

    // Twilight Forest charm events implementation
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (!event.isEndConquered()) {
            returnStoredItems(serverPlayer);
        }
    }

    private static void keepSoulboundItems(Player player) {
        Inventory keepInventory = new Inventory(null);
        ListTag tagList = new ListTag();

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (StackNBTUtil.isSoulbound(stack)) {
                keepInventory.items.set(i, stack.copy());
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }

        ItemStack offhandItemStack = player.getInventory().offhand.get(0);
        if (!offhandItemStack.isEmpty() && StackNBTUtil.isSoulbound(offhandItemStack)) {
            keepInventory.offhand.set(0, player.getInventory().offhand.get(0).copy());
            player.getInventory().offhand.set(0, ItemStack.EMPTY);
        }


        if (!keepInventory.isEmpty()) {
            keepInventory.save(tagList);
            getPlayerData(player).put(ARTIFACTORY_INV_TAG, tagList);
        }
    }

    // Twilight Forest charm events implementation
    private static void returnStoredItems(Player player) {
        // Check if our tag is in the persistent player data. If so, copy that inventory over to our own. Cloud storage at its finest!
        CompoundTag playerData = getPlayerData(player);
        if (!player.level().isClientSide() && playerData.contains(ARTIFACTORY_INV_TAG)) {
            ListTag tagList = playerData.getList(ARTIFACTORY_INV_TAG, 10);
            loadNoClear(tagList, player.getInventory());
            getPlayerData(player).getList(ARTIFACTORY_INV_TAG, 10).clear();
            getPlayerData(player).remove(ARTIFACTORY_INV_TAG);
        }
    }

    // Twilight Forest Implementation
    //[VanillaCopy] of Inventory.load, but removed clearing all slots
    //also add a handler to move items to the next available slot if the slot they want to go to isnt available
    private static void loadNoClear(ListTag tag, Inventory inventory) {

        List<ItemStack> blockedItems = new ArrayList<>();

        for (int i = 0; i < tag.size(); ++i) {
            CompoundTag compoundtag = tag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.of(compoundtag);
            if (!itemstack.isEmpty()) {
                if (j < inventory.items.size()) {
                    if (inventory.items.get(j).isEmpty()) {
                        inventory.items.set(j, itemstack);
                    } else {
                        blockedItems.add(itemstack);
                    }
                } else if (j >= 100 && j < inventory.armor.size() + 100) {
                    if (inventory.armor.get(j - 100).isEmpty()) {
                        inventory.armor.set(j - 100, itemstack);
                    } else {
                        blockedItems.add(itemstack);
                    }
                } else if (j >= 150 && j < inventory.offhand.size() + 150) {
                    if (inventory.offhand.get(j - 150).isEmpty()) {
                        inventory.offhand.set(j - 150, itemstack);
                    } else {
                        blockedItems.add(itemstack);
                    }
                }
            }
        }

        if(!blockedItems.isEmpty()) blockedItems.forEach(inventory::add);
    }

    public static CompoundTag getPlayerData(Player player) {
        if (!player.getPersistentData().contains(Player.PERSISTED_NBT_TAG)) {
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }
}
