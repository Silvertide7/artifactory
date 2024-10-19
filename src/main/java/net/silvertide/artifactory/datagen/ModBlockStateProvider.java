package net.silvertide.artifactory.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.registry.BlockRegistry;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Artifactory.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get(), new ModelFile.UncheckedModelFile(modLoc("block/attunement_nexus")));
    }
}
