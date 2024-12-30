package net.silvertide.artifactory.events.custom;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * InscribeSpellEvent is fired right before a {@link Player} attunes to an item.<br>
 * <br>
 * This event is Cancelable.<br>
 * If this event is canceled, the attunement fails.<br>
 * <br>
 * This event does not have a result.<br>
 * <br>
 * This event is fired on the NeoForge event bus (GAME)<br>
 **/
public class PreAttuneEvent extends PlayerEvent implements ICancellableEvent {
    private final ItemStack stack;

    public PreAttuneEvent(Player player, ItemStack stack)
    {
        super(player);
        this.stack = stack;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }
}
