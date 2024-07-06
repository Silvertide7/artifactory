package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.AttuneableItems;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.config.codecs.AttunementRequirements;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;

import java.util.*;

public final class DataPackUtil {
    private DataPackUtil() {}

    public static Optional<ItemAttunementData> getAttunementData(ItemStack stack) {
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return Optional.ofNullable(AttuneableItems.DATA_LOADER.getData().get(stackResourceLocation));
    }

    public static Optional<ItemAttunementData> getAttunementData(ResourceLocation resourceLocation) {
        return Optional.ofNullable(AttuneableItems.DATA_LOADER.getData().get(resourceLocation));
    }

    public static boolean hasAttunementData(ItemStack stack) {
        return getAttunementData(stack).isPresent();
    }

    public static Optional<AttunementLevel> getAttunementLevel(ItemStack stack, int level) {
        return getAttunementData(stack).flatMap(attunementData -> Optional.ofNullable(attunementData.attunements().get(String.valueOf(level))));
    }

    public static int getMaxLevelOfAttunementPossible(ItemStack stack) {
        return getAttunementData(stack).map(attunementData -> {
            int maxLevel = 0;
            for (String key : attunementData.attunements().keySet()) {
                int currentLevel = Integer.parseInt(key);
                if(currentLevel > maxLevel) maxLevel = currentLevel;
            }

            return maxLevel;
        }).orElse(0);
    }

    public static Optional<AttunementRequirements> getAttunementRequirements(ItemStack stack, int level) {
        return getAttunementLevel(stack, level).map(AttunementLevel::requirements);
    }
    
    public static Optional<List<String>> getAttunementModifications(ItemStack stack, int level) {
        return getAttunementLevel(stack, level).map(AttunementLevel::modifications);
    }
}
