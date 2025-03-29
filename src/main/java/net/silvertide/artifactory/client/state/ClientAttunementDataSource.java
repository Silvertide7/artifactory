package net.silvertide.artifactory.client.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.util.*;

import java.util.Map;
import java.util.Optional;

public class ClientAttunementDataSource {
    private static Map<ResourceLocation, AttunementDataSource> attunementDataSource = null;
    private ClientAttunementDataSource() {}

    public static void setClientAttunementDataSource(Map<ResourceLocation, AttunementDataSource> syncedAttunementData) {
        attunementDataSource = syncedAttunementData;
    }

    public static Optional<AttunementDataSource> getClientAttunementDataSource(ResourceLocation resourceLocation) {
        if(attunementDataSource == null) return Optional.empty();
        return Optional.ofNullable(attunementDataSource.get(resourceLocation));
    }

    public static Optional<AttunementDataSource> getClientAttunementDataSource(ItemStack stack) {
        if(attunementDataSource == null) return Optional.empty();
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return getClientAttunementDataSource(stackResourceLocation);
    }

    public static Optional<AttunementDataSource> getClientAttunementDataSource(String resourceLocation) {
        if(attunementDataSource == null) return Optional.empty();
        return getClientAttunementDataSource(ResourceLocation.parse(resourceLocation));
    }
}
