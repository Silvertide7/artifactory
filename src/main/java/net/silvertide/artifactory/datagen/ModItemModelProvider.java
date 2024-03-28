package net.silvertide.artifactory.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.silvertide.artifactory.Artifactory;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Artifactory.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
    }

    public void buttonItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        fromBaseItem(block, baseBlock, "block/button_inventory", "texture");
    }

    public void fenceItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        fromBaseItem(block, baseBlock, "block/fence_inventory", "texture");
    }

    public void wallItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        fromBaseItem(block, baseBlock, "block/wall_inventory", "wall");
    }


    public void fromBaseItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock, String mcLoc, String texture) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(), mcLoc(mcLoc))
                .texture(texture, new ResourceLocation(Artifactory.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
            new ResourceLocation("item/generated")).texture("layer0", new ResourceLocation(Artifactory.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0", new ResourceLocation(Artifactory.MOD_ID, "item/" + item.getId().getPath()));
    }
}
