package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.config.codecs.AttunableItems;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.config.codecs.AttunementRequirements;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;

import java.util.*;

public final class DataPackUtil {
    private DataPackUtil() {}

    public static Optional<ItemAttunementData> getAttunementData(ResourceLocation resourceLocation) {
        return Optional.ofNullable(AttunableItems.DATA_LOADER.getData().get(resourceLocation));
    }

    public static Optional<ItemAttunementData> getAttunementData(ItemStack stack) {
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return Optional.ofNullable(AttunableItems.DATA_LOADER.getData().get(stackResourceLocation));
    }

    public static Optional<ItemAttunementData> getAttunementData(String resourceLocation) {
        return getAttunementData(ResourceLocationUtil.getResourceLocation(resourceLocation));
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

    public static String getAttunementLevelDescriptions(String resourceLocation, int currentAttunementLevel) {
        // "minecraft:diamond_sword;1#soulbound,invulnerable,attack~2#unbreakable"
        return getAttunementData(resourceLocation).map(itemAttunementData -> {
            StringBuilder stringBuilder = new StringBuilder(resourceLocation + ";");

            Iterator<Map.Entry<String, AttunementLevel>> iterator = itemAttunementData.attunements().entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, AttunementLevel> entry = iterator.next();
                if(shouldSendAttunementLevelInformation(entry.getKey(), currentAttunementLevel)){
                    stringBuilder.append(entry.getKey()).append("#").append(entry.getValue().getModifications());
                    if (iterator.hasNext()) stringBuilder.append("~");
                }


            }
            return stringBuilder.toString();
        }).orElse("");
    }

    private static boolean shouldSendAttunementLevelInformation(String level, int currentAttunementLevel) {
        String currentInformationLevel = Config.ATTUNEMENT_INFORMATION_EXTENT.get();
        if("all".equals(currentInformationLevel)) return true;
        else {
            try {
                int informationLevel = Integer.parseInt(level);
                if("next".equals(currentInformationLevel)) return informationLevel <= currentAttunementLevel + 1;
                if("current".equals(currentInformationLevel)) return informationLevel <= currentAttunementLevel;
            } catch (NumberFormatException exception) {
                Artifactory.LOGGER.error("Error converting datapack attunement level to integer.");
                return false;
            }
        }
        return false;
    }
}
