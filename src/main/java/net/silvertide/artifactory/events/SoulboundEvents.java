package net.silvertide.artifactory.events;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.AttunementUtil;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Artifactory.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SoulboundEvents {
    public static final String ARTIFACTORY_INV_TAG = "artifactoryInventory";

    // Most of the implementation for keeping items in the inventory was taken from Twilight Forests charm of keeping.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeath(LivingDeathEvent livingDeathEvent) {
        LivingEntity livingEntity = livingDeathEvent.getEntity();
        if (!(livingDeathEvent.getEntity() instanceof ServerPlayer serverPlayer) || livingDeathEvent.isCanceled() || !(livingEntity instanceof Player player)
                || livingEntity instanceof FakePlayer || player.isSpectator()) return;

        if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            keepSoulboundItems(serverPlayer);
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

    private static void keepSoulboundItems(ServerPlayer serverPlayer) {
        Inventory keepInventory = new Inventory(serverPlayer);
        ListTag tagList = new ListTag();

        for (int i = 0; i < serverPlayer.getInventory().items.size(); i++) {
            ItemStack stack = serverPlayer.getInventory().items.get(i);
            if(AttunementUtil.isSoulboundActive(serverPlayer, stack)) {
                keepInventory.items.set(i, stack.copy());
                serverPlayer.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < serverPlayer.getInventory().armor.size(); i++) {
            ItemStack armorStack = serverPlayer.getInventory().armor.get(i);
            if(AttunementUtil.isSoulboundActive(serverPlayer, armorStack)) {
                keepInventory.armor.set(i, armorStack.copy());
                serverPlayer.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }

        ItemStack offhandStack = serverPlayer.getInventory().offhand.getFirst();
        if (AttunementUtil.isSoulboundActive(serverPlayer, offhandStack)) {
            keepInventory.offhand.set(0, serverPlayer.getInventory().offhand.getFirst().copy());
            serverPlayer.getInventory().offhand.set(0, ItemStack.EMPTY);
        }

        if (!keepInventory.isEmpty()) {
            keepInventory.save(tagList);
            getPlayerData(serverPlayer).put(ARTIFACTORY_INV_TAG, tagList);
        }
    }

    // Twilight Forest charm events implementation
    private static void returnStoredItems(Player player) {
        // Check if our tag is in the persistent player data. If so, copy that inventory over to our own. Cloud storage at its finest!
        CompoundTag playerData = getPlayerData(player);
        if (!player.level().isClientSide() && playerData.contains(ARTIFACTORY_INV_TAG)) {
            ListTag tagList = playerData.getList(ARTIFACTORY_INV_TAG, 10);
            loadNoClear(player.registryAccess(), tagList, player.getInventory());
            getPlayerData(player).getList(ARTIFACTORY_INV_TAG, 10).clear();
            getPlayerData(player).remove(ARTIFACTORY_INV_TAG);
        }
    }

    //[VanillaCopy] of Inventory.load, but removed clearing all slots
    //also add a handler to move items to the next available slot if the slot they want to go to isnt available
    public static void loadNoClear(RegistryAccess registryAccess, ListTag tag, Inventory inventory) {

        List<ItemStack> blockedItems = new ArrayList<>();

        for (int i = 0; i < tag.size(); ++i) {
            CompoundTag compoundtag = tag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.parseOptional(registryAccess, compoundtag);
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

        if (!blockedItems.isEmpty()) blockedItems.forEach(inventory::add);
    }

    public static CompoundTag getPlayerData(Player player) {
        if (!player.getPersistentData().contains(Player.PERSISTED_NBT_TAG)) {
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }
}
