package net.silvertide.artifactory.events.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * InscribeSpellEvent is fired after a {@link Player} attunes to an item.<br>
 * <br>
 * This event is not Cancelable.<br>
 * <br>
 * This event does not have a result.<br>
 * <br>
 * This event is fired on the Neoforge Event Bus (GAME).<br>
 **/
public class PostAttuneEvent extends PlayerEvent implements ICancellableEvent {
    private final ItemStack stack;

    public PostAttuneEvent(Player player, ItemStack stack)
    {
        super(player);
        this.stack = stack;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }
}
