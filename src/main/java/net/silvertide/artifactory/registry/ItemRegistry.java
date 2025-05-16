package net.silvertide.artifactory.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.artifactory.Artifactory;

import java.util.Collection;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Artifactory.MOD_ID);
    public static final DeferredHolder<Item, Item> ATTUNEMENT_NEXUS_BLOCK_ITEM = ITEMS.register("attunement_nexus", () -> new BlockItem(BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get(), new Item.Properties()));
    public static void register(IEventBus eventBus) { ITEMS.register(eventBus); }
}
