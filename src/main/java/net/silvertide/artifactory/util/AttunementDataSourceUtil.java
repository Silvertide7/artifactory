package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.AttunableItems;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;

import java.util.*;

public final class AttunementDataSourceUtil {
    private AttunementDataSourceUtil() {}

    public static Optional<Map<ResourceLocation, AttunementDataSource>> getAttunementDataMap() {
        Map<ResourceLocation, AttunementDataSource> data = AttunableItems.getActiveData();
        if(data.isEmpty()) return Optional.empty();
        return Optional.of(data);
    }
    public static Optional<AttunementDataSource> getAttunementDataSource(ItemStack stack) {
        return AttunableItems.getActiveData(ResourceLocationUtil.getResourceLocation(stack));
    }

    public static Optional<AttunementDataSource> getAttunementDataSource(String resourceLocation) {
        return getAttunementDataSource(ResourceLocation.parse(resourceLocation));
    }

    private static Optional<AttunementDataSource> getAttunementDataSource(ResourceLocation resourceLocation) {
        return AttunableItems.getActiveData(resourceLocation);
    }
}
