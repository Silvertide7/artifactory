package net.silvertide.artifactory.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.silverlib.feature.soulbound.SoulboundCheckEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Artifactory.MOD_ID)
public class SoulboundEvents {
    private static final String LEGACY_STASH_TAG = "artifactoryInventory";
    private static final int LEGACY_ARMOR_SLOT_OFFSET = 100;
    private static final int LEGACY_OFFHAND_SLOT = 150;
    private static final int COMBINED_ARMOR_SLOT_START = 36;
    private static final int COMBINED_OFFHAND_SLOT = 40;

    @SubscribeEvent
    public static void onSoulboundCheck(SoulboundCheckEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && !(player instanceof FakePlayer) && AttunementUtil.isSoulboundActive(player, event.getStack())) {
            event.keep();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.isDeadOrDying()) returnLegacyStash(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) returnLegacyStash(player);
    }

    private static void returnLegacyStash(ServerPlayer player) {
        CompoundTag persistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persistedData.contains(LEGACY_STASH_TAG)) return;
        ListTag legacyStash = persistedData.getList(LEGACY_STASH_TAG, Tag.TAG_COMPOUND);
        persistedData.remove(LEGACY_STASH_TAG);

        Inventory inventory = player.getInventory();
        List<ItemStack> displacedStacks = new ArrayList<>();
        for (int i = 0; i < legacyStash.size(); i++) {
            CompoundTag entry = legacyStash.getCompound(i);
            ItemStack stack = ItemStack.parseOptional(player.registryAccess(), entry);
            if (stack.isEmpty()) {
                Artifactory.LOGGER.warn("Dropping unreadable soulbound item kept by Artifactory before 2.7.0 for {}: {}", player.getName().getString(), entry);
                continue;
            }
            int combinedSlot = toCombinedInventorySlot(entry.getByte("Slot") & 255);
            if (combinedSlot >= 0 && combinedSlot < inventory.getContainerSize() && inventory.getItem(combinedSlot).isEmpty()) {
                inventory.setItem(combinedSlot, stack);
            } else {
                displacedStacks.add(stack);
            }
        }
        displacedStacks.forEach(stack -> {
            if (inventory.getFreeSlot() >= 0) inventory.add(stack);
            if (!stack.isEmpty()) player.drop(stack, false, false);
        });
    }

    private static int toCombinedInventorySlot(int legacySlot) {
        if (legacySlot == LEGACY_OFFHAND_SLOT) return COMBINED_OFFHAND_SLOT;
        if (legacySlot >= LEGACY_ARMOR_SLOT_OFFSET) return COMBINED_ARMOR_SLOT_START + legacySlot - LEGACY_ARMOR_SLOT_OFFSET;
        return legacySlot;
    }
}
