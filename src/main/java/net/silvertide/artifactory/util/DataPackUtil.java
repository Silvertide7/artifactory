package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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

    public static AttunementLevel getAttunementLevel(ItemStack stack, int level) {
        return getAttunementData(stack).map(attunementData -> attunementData.attunementLevels().get(level - 1)).orElse(null);
    }

    public static int getMaxLevelOfAttunementPossible(ItemStack stack) {
        return getAttunementData(stack).map(attunementData -> attunementData.attunementLevels().size()).orElse(0);
    }

    public static String getAttunementLevelDescriptions(String resourceLocation, int currentAttunementLevel) {
        // "minecraft:diamond_sword;1#soulbound,invulnerable,attack~2#unbreakable"
        return getAttunementData(resourceLocation).map(itemAttunementData -> {
            StringBuilder stringBuilder = new StringBuilder(resourceLocation + ";");

            for(int i = 0; i < itemAttunementData.attunementLevels().size(); i++) {
                AttunementLevel attunementLevel = itemAttunementData.attunementLevels().get(i);
                if(shouldSendAttunementLevelInformation(i, currentAttunementLevel)){
                    stringBuilder.append(i).append("#").append(attunementLevel.getModifications());
                    if (i != itemAttunementData.attunementLevels().size() - 1) stringBuilder.append("~");
                }
            }
            return stringBuilder.toString();
        }).orElse("");
    }

    private static boolean shouldSendAttunementLevelInformation(int level, int currentAttunementLevel) {
        String currentInformationLevel = Config.ATTUNEMENT_INFORMATION_EXTENT.get();
        if("all".equals(currentInformationLevel)) return true;
        else {
            if("next".equals(currentInformationLevel)) return level <= currentAttunementLevel + 1;
            if("current".equals(currentInformationLevel)) return level <= currentAttunementLevel;
        }
        return false;
    }
}
