package net.silvertide.artifactory.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.silvertide.artifactory.component.AttunementFlag;
import net.silvertide.artifactory.component.AttunementOverride;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.registry.DataComponentRegistry;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.ArrayList;
import java.util.Optional;

public final class DataComponentUtil {
    private DataComponentUtil() {}

    // ATTUNEMENT FLAG METHODS
    public static Optional<AttunementFlag> getAttunementFlag(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponentRegistry.ATTUNEMENT_FLAG));
    }

    public static void setAttunementFlag(ItemStack stack, AttunementFlag attunementFlag) {
        stack.set(DataComponentRegistry.ATTUNEMENT_FLAG, attunementFlag);
    }

    // ATTUNEMENT OVERRIDE METHODS
    public static Optional<AttunementOverride> getAttunementOverride(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponentRegistry.ATTUNEMENT_OVERRIDE));
    }

    // PLAYER ATTUNEMENT DATA METHODS
    public static Optional<PlayerAttunementData> getPlayerAttunementData(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponentRegistry.PLAYER_ATTUNEMENT_DATA));
    }

    public static void setPlayerAttunementData(ItemStack stack, PlayerAttunementData playerAttunementData) {
        stack.set(DataComponentRegistry.PLAYER_ATTUNEMENT_DATA, playerAttunementData);
    }

    public static void clearPlayerAttunementData(ItemStack stack) {
        setPlayerAttunementData(stack, null);
    }

    // LIFECYCLE METHODS

    public static void configurePlayerAttunementData(ServerPlayer player, ItemStack stack, AttunedItem attunedItem) {
        setPlayerAttunementData(stack,
                new PlayerAttunementData(
                        attunedItem.getItemUUID(),
                        player.getUUID(),
                        player.getDisplayName().getString(),
                        false,
                        false,
                        false,
                        new ArrayList<>()
                )
        );
    }

    // EXISTING DATACOMPONENT METHODS
    public static void makeUnbreakable(ItemStack stack) {
        if(!isUnbreakable(stack)) {
            stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
            Integer damage = stack.get(DataComponents.DAMAGE);
            if(damage != null && damage > 0) stack.set(DataComponents.DAMAGE, 0);

            // Turn on the unbreakable flag because we only hit this code if it wasn't unbreakable
            // before hand. This will remove unbreakable in the future if they break the attunement.
            getPlayerAttunementData(stack).ifPresent(attunementData -> {
                setPlayerAttunementData(stack, attunementData.withIsUnbreakable(true));
            });
        }
    }

    public static boolean isUnbreakable(ItemStack stack) {
        return stack.get(DataComponents.UNBREAKABLE) != null;
    }

    public static void removeUnbreakable(ItemStack stack) {
        stack.set(DataComponents.UNBREAKABLE, null);
    }

    public static Optional<String> getItemDisplayName(ItemStack stack) {
       Component customName = stack.get(DataComponents.CUSTOM_NAME);
       if(customName != null) return Optional.of(customName.getString());

       Component itemName = stack.get(DataComponents.ITEM_NAME);
       if(itemName != null) return Optional.of(itemName.getString());

       return Optional.empty();
    }

}