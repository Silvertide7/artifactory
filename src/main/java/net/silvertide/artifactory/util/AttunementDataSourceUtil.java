package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.config.codecs.AttunableItems;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.component.AttunementLevel;

import java.util.*;

public final class AttunementDataSourceUtil {
    private AttunementDataSourceUtil() {}

    public static Optional<Map<ResourceLocation, AttunementDataSource>> getAttunementDataMap() {
        if(AttunableItems.DATA_LOADER.getData().isEmpty()) return Optional.empty();
        return Optional.of(AttunableItems.DATA_LOADER.getData());
    }

    public static Optional<AttunementDataSource> getAttunementDataSource(ResourceLocation resourceLocation) {
        return Optional.ofNullable(AttunableItems.DATA_LOADER.getData().get(resourceLocation));
    }

    public static Optional<AttunementDataSource> getAttunementDataSource(ItemStack stack) {
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return Optional.ofNullable(AttunableItems.DATA_LOADER.getData().get(stackResourceLocation));
    }

    public static Optional<AttunementDataSource> getAttunementDataSource(String resourceLocation) {
        return getAttunementDataSource(ResourceLocation.parse(resourceLocation));
    }

    public static String getAttunementLevelDescriptions(String resourceLocation, int currentAttunementLevel) {
        // "minecraft:diamond_sword;1#soulbound,invulnerable,attack~2#unbreakable"
        return getAttunementDataSource(resourceLocation).map(itemAttunementData -> {
            StringBuilder stringBuilder = new StringBuilder(resourceLocation + ";");

            for(int i = 0; i < itemAttunementData.attunementLevels().size(); i++) {
                AttunementLevel attunementLevel = itemAttunementData.attunementLevels().get(i);
                if(shouldSendAttunementLevelInformation(i, currentAttunementLevel)){
                    stringBuilder.append(i+1).append("#").append(attunementLevel.getModificationsStringList());
                    if (i != itemAttunementData.attunementLevels().size() - 1) stringBuilder.append("~");
                }
            }
            return stringBuilder.toString();
        }).orElse("");
    }

    private static boolean shouldSendAttunementLevelInformation(int level, int currentAttunementLevel) {
        String currentInformationLevel = ServerConfigs.ATTUNEMENT_INFORMATION_EXTENT.get();
        if("all".equals(currentInformationLevel)) return true;
        else {
            if("next".equals(currentInformationLevel)) return level < currentAttunementLevel + 1;
            if("current".equals(currentInformationLevel)) return level <= currentAttunementLevel;
        }
        return false;
    }

    public static boolean canUseWithoutAttunement(ItemStack stack) {
        return getAttunementDataSource(stack).map(AttunementDataSource::useWithoutAttunement).orElse(false);
    }

    public static boolean isUniqueAttunement(ItemStack stack) {
        return getAttunementDataSource(stack).map(AttunementDataSource::unique).orElse(false);
    }

    public static boolean isUniqueAttunement(String resourceLocation) {
        return getAttunementDataSource(resourceLocation).map(AttunementDataSource::unique).orElse(false);
    }
}
