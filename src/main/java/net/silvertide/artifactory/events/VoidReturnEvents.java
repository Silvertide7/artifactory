package net.silvertide.artifactory.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.services.AttunementService;
import net.silvertide.artifactory.services.PlayerMessenger;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataComponentUtil;
import net.silvertide.artifactory.util.InventoryUtil;

import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = Artifactory.MOD_ID)
public class VoidReturnEvents {
    public static boolean returnAttunedItemToOwner(ItemEntity itemEntity) {
        if (!(itemEntity.level() instanceof ServerLevel level)) return false;
        ItemStack stack = itemEntity.getItem();
        if (!AttunementUtil.isItemAttunedToAPlayer(stack)) return false;
        AttunementService.checkAndUpdateAttunementComponents(stack);
        Optional<UUID> ownerUUID = DataComponentUtil.getPlayerAttunementData(stack).map(PlayerAttunementData::attunedToUUID);
        if (ownerUUID.isEmpty()) return false;
        returnToOwner(level, ownerUUID.get(), stack.copy());
        itemEntity.discard();
        return true;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.isDeadOrDying()) giveItemsWaitingForOwner(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) giveItemsWaitingForOwner(player);
    }

    private static void returnToOwner(ServerLevel level, UUID ownerUUID, ItemStack stack) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null && !owner.isDeadOrDying()) {
            giveBack(owner, stack);
        } else {
            ArtifactorySavedData.get().keepItemReturnedFromVoid(ownerUUID, stack);
        }
    }

    private static void giveItemsWaitingForOwner(ServerPlayer player) {
        ArtifactorySavedData.get().takeItemsReturnedFromVoid(player.getUUID()).forEach(stack -> giveBack(player, stack));
    }

    private static void giveBack(ServerPlayer owner, ItemStack stack) {
        String displayName = AttunementUtil.getAttunedItemDisplayName(stack);
        InventoryUtil.giveOrDropAtFeet(owner, stack);
        PlayerMessenger.displayTranslatableClientMessage(owner, "playermessage.artifactory.returned_from_void", displayName);
    }
}
