package net.silvertide.artifactory.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.registry.BlockRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Artifactory.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get());

        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get());
    }

    @Override
    public String getName() {
        return "Block Tags";
    }
}
