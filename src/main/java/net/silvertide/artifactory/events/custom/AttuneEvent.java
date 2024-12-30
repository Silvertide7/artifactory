package net.silvertide.artifactory.events.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public abstract class AttuneEvent extends PlayerEvent {
        private final ItemStack stack;

        public AttuneEvent(Player player, ItemStack stack) {
            super(player);
            this.stack = stack;
        }

        public ItemStack getItemStack() {
            return this.stack;
        }

        public static class Pre extends AttuneEvent implements ICancellableEvent {
            public Pre(Player player, ItemStack item) {
                super(player, item);
            }
        }

        public static class Post extends AttuneEvent {
            public Post(Player player, ItemStack stack) {
                super(player, stack);
            }
        }
}
