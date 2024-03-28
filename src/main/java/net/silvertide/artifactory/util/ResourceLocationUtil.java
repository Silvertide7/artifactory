package net.silvertide.artifactory.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;

public final class ResourceLocationUtil {
    private ResourceLocationUtil() {}

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(Artifactory.MOD_ID, path);
    }

    public static ResourceLocation getResourceLocation(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

}
