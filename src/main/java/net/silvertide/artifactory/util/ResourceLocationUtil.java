package net.silvertide.artifactory.util;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;

public final class ResourceLocationUtil {
    private ResourceLocationUtil() {}

    public static ResourceLocation getResourceLocation(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static Item getItemFromResourceLocation(String stringifiedResourceLocation) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(stringifiedResourceLocation));
    }
    public static Item getItemFromResourceLocation(ResourceLocation location) {
        return BuiltInRegistries.ITEM.get(location);
    }

    public static ItemStack getItemStackFromResourceLocation(String resourceLocation) {
        try {
            return getItemStackFromResourceLocation(ResourceLocation.parse(resourceLocation));
        } catch (ResourceLocationException exception) {
            Artifactory.LOGGER.error("Artifactory - getItemStackFromResourceLocation - Could not get item from {}", resourceLocation);
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack getItemStackFromResourceLocation(ResourceLocation resourceLocation) {
        try {
            Item baseItem = getItemFromResourceLocation(resourceLocation);
            return new ItemStack(baseItem);
        } catch (ResourceLocationException exception) {
            Artifactory.LOGGER.error("Artifactory - getItemStackFromResourceLocation - Could not get item from {}", resourceLocation);
            return ItemStack.EMPTY;
        }
    }

    public static String prettifyName(String resourceLocation) {
        String prettifiedLocation = resourceLocation;
        if(prettifiedLocation.contains(":")) {
            prettifiedLocation = resourceLocation.split(":")[1];
        }

        if(prettifiedLocation.contains(".")) {
            String[] periodSeperated = prettifiedLocation.split("\\.");
            prettifiedLocation = periodSeperated[periodSeperated.length - 1];
        }

        if(prettifiedLocation.contains("_")) {
            StringBuilder result = new StringBuilder();
            String[] words = prettifiedLocation.split("_");
            for (String word : words) {
                if(word.isEmpty()) continue;
                String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                if(!result.isEmpty()) result.append(" ");
                result.append(capitalizedWord);
            }
            prettifiedLocation = result.toString();
        } else if(!prettifiedLocation.isEmpty() && !prettifiedLocation.contains(" ")) {
            prettifiedLocation = prettifiedLocation.substring(0, 1).toUpperCase() + prettifiedLocation.substring(1).toLowerCase();
        }

        return prettifiedLocation;
    }
}
