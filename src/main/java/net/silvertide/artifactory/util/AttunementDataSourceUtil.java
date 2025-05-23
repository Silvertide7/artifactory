package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.AttunableItems;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;

import java.util.*;

public final class AttunementDataSourceUtil {
    private AttunementDataSourceUtil() {}

    public static Optional<Map<ResourceLocation, AttunementDataSource>> getAttunementDataMap() {
        if(AttunableItems.DATA_LOADER.getData().isEmpty()) return Optional.empty();
        return Optional.of(AttunableItems.DATA_LOADER.getData());
    }
    public static Optional<AttunementDataSource> getAttunementDataSource(ItemStack stack) {
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return Optional.ofNullable(AttunableItems.DATA_LOADER.getData().get(stackResourceLocation));
    }

    public static Optional<AttunementDataSource> getAttunementDataSource(String resourceLocation) {
        return getAttunementDataSource(ResourceLocation.parse(resourceLocation));
    }

    private static Optional<AttunementDataSource> getAttunementDataSource(ResourceLocation resourceLocation) {
        return Optional.ofNullable(AttunableItems.DATA_LOADER.getData().get(resourceLocation));
    }
}
