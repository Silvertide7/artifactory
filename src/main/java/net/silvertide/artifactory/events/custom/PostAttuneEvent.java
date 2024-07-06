package net.silvertide.artifactory.events.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * InscribeSpellEvent is fired after a {@link Player} attunes to an item.<br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 **/
public class PostAttuneEvent extends PlayerEvent {
    private final ItemStack stack;

    public PostAttuneEvent(Player player, ItemStack stack)
    {
        super(player);
        this.stack = stack;
    }

    @Override
    public boolean isCancelable() { return true; }

    public ItemStack getItemStack() {
        return this.stack;
    }
}
