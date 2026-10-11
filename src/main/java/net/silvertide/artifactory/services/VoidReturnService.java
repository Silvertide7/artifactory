package net.silvertide.artifactory.services;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataComponentUtil;
import net.silvertide.artifactory.util.InventoryUtil;

import java.util.Optional;
import java.util.UUID;

public final class VoidReturnService {
    private VoidReturnService() {}

    public static boolean returnAttunedItemToOwner(Entity entity) {
        if (entity.isRemoved() || !(entity.level() instanceof ServerLevel level)) return false;
        ItemStack stack = itemCarriedBy(entity);
        if (!AttunementUtil.isItemAttunedToAPlayer(stack)) return false;
        AttunementService.checkAndUpdateAttunementComponents(stack);
        Optional<UUID> ownerUUID = DataComponentUtil.getPlayerAttunementData(stack).map(PlayerAttunementData::attunedToUUID);
        if (ownerUUID.isEmpty()) return false;
        returnToOwner(level, ownerUUID.get(), stack.copy());
        entity.discard();
        return true;
    }

    public static void giveItemsWaitingForOwner(ServerPlayer player) {
        ArtifactorySavedData.get().takeItemsReturnedFromVoid(player.getUUID()).forEach(stack -> giveBack(player, stack));
    }

    private static ItemStack itemCarriedBy(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) return itemEntity.getItem();
        if (entity instanceof AbstractArrow arrow && arrow.pickup == AbstractArrow.Pickup.ALLOWED) return arrow.getPickupItemStackOrigin();
        return ItemStack.EMPTY;
    }

    private static void returnToOwner(ServerLevel level, UUID ownerUUID, ItemStack stack) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null && !owner.isDeadOrDying()) {
            giveBack(owner, stack);
        } else {
            ArtifactorySavedData.get().keepItemReturnedFromVoid(ownerUUID, stack);
        }
    }

    private static void giveBack(ServerPlayer owner, ItemStack stack) {
        String displayName = AttunementUtil.getAttunedItemDisplayName(stack);
        InventoryUtil.giveOrDropAtFeet(owner, stack);
        PlayerMessenger.sendSystemMessage(owner, Component.translatable("playermessage.artifactory.returned_from_void", displayName));
    }
}
