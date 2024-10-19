package net.silvertide.artifactory.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.silvertide.artifactory.registry.BlockRegistry;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get())
                .define('A', Ingredient.of(Items.ANVIL))
                .define('B', Ingredient.of(Items.BEACON))
                .define('C', Ingredient.of(Items.CONDUIT))
                .define('S', Ingredient.of(Items.BLACKSTONE))
                .define('W', Ingredient.of(Items.DARK_OAK_WOOD))
                .pattern("WAW")
                .pattern("SBS")
                .pattern("SCS")
                .unlockedBy("has_beacon", has(Items.BEACON))
                .save(pWriter);
    }
}
