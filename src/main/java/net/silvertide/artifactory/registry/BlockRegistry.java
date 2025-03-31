package net.silvertide.artifactory.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.blocks.AttunementNexusBlock;

import java.util.Collection;

public class BlockRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Artifactory.MOD_ID);
    public static final DeferredHolder<Block, Block> ATTUNEMENT_NEXUS_BLOCK = BLOCKS.register("attunement_nexus", AttunementNexusBlock::new);
    public static Collection<DeferredHolder<Block, ? extends Block>> blocks() {
        return BLOCKS.getEntries();
    }
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
