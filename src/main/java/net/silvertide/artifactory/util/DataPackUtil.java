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
    public static boolean hasAttunementData(ResourceLocation resourceLocation) {
        return getAttunementData(resourceLocation).isPresent();
    }

    public static Optional<AttunementLevel> getAttunementLevel(ItemStack stack, int level) {
        return getAttunementData(stack).flatMap(attunementData -> Optional.ofNullable(attunementData.attunements().get(String.valueOf(level))));
    }

    public static int getMaxLevelOfAttunementPossible(ItemStack stack) {
        return getAttunementData(stack).map(DataPackUtil::findMaxLevelAchievable).orElse(0);
    }

    public static int getMaxLevelOfAttunementPossible(ResourceLocation resourceLocation) {
        return getAttunementData(resourceLocation).map(DataPackUtil::findMaxLevelAchievable).orElse(0);
    }

    private static int findMaxLevelAchievable(ItemAttunementData attunementData) {
        int maxLevel = 0;
        Set<String> attunementLevels = attunementData.attunements().keySet();
        for(int i = 1; i <= attunementLevels.size(); i++) {
            if(attunementLevels.contains(String.valueOf(i))){
                maxLevel = i;
            } else {
                return maxLevel;
            }
        }

        return maxLevel;
    }

    public static Optional<AttunementRequirements> getAttunementRequirements(ItemStack stack, int level) {
        return getAttunementLevel(stack, level).map(AttunementLevel::requirements);
    }
    
    public static Optional<List<String>> getAttunementModifications(ItemStack stack, int level) {
        return getAttunementLevel(stack, level).map(AttunementLevel::modifications);
    }
}
