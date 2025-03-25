package net.silvertide.artifactory.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.registry.DataComponentRegistry;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public final class DataComponentUtil {
    private DataComponentUtil() {}

    // ACCESS METHODS
    public static Optional<PlayerAttunementData> getAttunementData(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponentRegistry.PLAYER_ATTUNEMENT_DATA));
    }

    public static void setAttunementData(ItemStack stack, PlayerAttunementData playerAttunementData) {
        stack.set(DataComponentRegistry.PLAYER_ATTUNEMENT_DATA, playerAttunementData);
    }

    public static void clearAttunementData(ItemStack stack) {
        setAttunementData(stack, null);
    }

    // LIFECYCLE METHODS
    public static void setupAttunementData(ItemStack stack) {
        setAttunementData(stack,
                new PlayerAttunementData(
                        UUID.randomUUID(),
                    null,
                    null,
                    false,
                    false,
                    false,
                    new ArrayList<>()
                )
        );
    }

    public static void configureAttunementData(ServerPlayer player, ItemStack stack) {
        getAttunementData(stack).ifPresent(attunementData -> {
            setAttunementData(stack, attunementData.withAttunedToUUID(player.getUUID()).withAttunedToName(player.getDisplayName().getString()));
        });
    }

    // EXISTING DATACOMPONENT METHODS
    public static void makeUnbreakable(ItemStack stack) {
        if(!isUnbreakable(stack)) {
            stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
            Integer damage = stack.get(DataComponents.DAMAGE);
            if(damage != null && damage > 0) stack.set(DataComponents.DAMAGE, 0);

            // Turn on the unbreakable flag because we only hit this code if it wasn't unbreakable
            // before hand. This will remove unbreakable in the future if they break the attunement.
            getAttunementData(stack).ifPresent(attunementData -> {
                setAttunementData(stack, attunementData.withIsUnbreakable(true));
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