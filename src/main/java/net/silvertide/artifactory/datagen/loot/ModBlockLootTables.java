package net.silvertide.artifactory.datagen.loot;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import net.silvertide.artifactory.registry.BlockRegistry;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
//        this.dropSelf(BlockRegistry.ALEXANDRITE_BLOCK.get());
//        this.dropSelf(BlockRegistry.RAW_ALEXANDRITE_BLOCK.get());
//        this.dropSelf(BlockRegistry.SOUND_BLOCK.get());
//
//        this.add(BlockRegistry.ALEXANDRITE_ORE.get(), block -> createOreDrop(BlockRegistry.ALEXANDRITE_ORE.get(), ModItems.RAW_ALEXANDRITE.get()));
//        this.add(BlockRegistry.DEEPSLATE_ALEXANDRITE_ORE.get(), block -> createOreDrop(BlockRegistry.DEEPSLATE_ALEXANDRITE_ORE.get(), ModItems.RAW_ALEXANDRITE.get()));
//        this.add(BlockRegistry.END_STONE_ALEXANDRITE_ORE.get(), block -> createOreDrop(BlockRegistry.END_STONE_ALEXANDRITE_ORE.get(), ModItems.RAW_ALEXANDRITE.get()));
//        this.add(BlockRegistry.NETHER_ALEXANDRITE_ORE.get(), block -> createOreDrop(BlockRegistry.NETHER_ALEXANDRITE_ORE.get(), ModItems.RAW_ALEXANDRITE.get()));
//        this.add(BlockRegistry.ALEXANDRITE_SLAB.get(), block -> createSlabItemTable(BlockRegistry.ALEXANDRITE_SLAB.get()));
//
//        this.dropSelf(BlockRegistry.ALEXANDRITE_STAIRS.get());
//        this.dropSelf(BlockRegistry.ALEXANDRITE_PRESSURE_PLATE.get());
//        this.dropSelf(BlockRegistry.ALEXANDRITE_BUTTON.get());
//        this.dropSelf(BlockRegistry.ALEXANDRITE_FENCE.get());
//        this.dropSelf(BlockRegistry.ALEXANDRITE_FENCE_GATE.get());
//        this.dropSelf(BlockRegistry.ALEXANDRITE_WALL.get());
//        this.dropSelf(BlockRegistry.ALEXANDRITE_TRAP_DOOR.get());
//
//        this.add(BlockRegistry.ALEXANDRITE_DOOR.get(), block -> createDoorTable(BlockRegistry.ALEXANDRITE_DOOR.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BlockRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
