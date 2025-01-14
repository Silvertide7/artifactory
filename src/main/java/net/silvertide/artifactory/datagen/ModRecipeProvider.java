package net.silvertide.artifactory.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.silvertide.artifactory.registry.BlockRegistry;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get())
                .pattern("SAS")
                .pattern("WBW")
                .pattern("WCW")
                .define('A', Ingredient.of(Items.ANVIL))
                .define('B', Ingredient.of(Items.BEACON))
                .define('C', Ingredient.of(Items.CONDUIT))
                .define('S', Ingredient.of(Items.BLACKSTONE))
                .define('W', Ingredient.of(Items.DARK_OAK_WOOD))
                .unlockedBy("has_beacon", has(Items.BEACON)).save(recipeOutput);
    }
}