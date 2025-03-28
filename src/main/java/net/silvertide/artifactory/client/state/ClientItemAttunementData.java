package net.silvertide.artifactory.client.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.AttunementFlag;
import net.silvertide.artifactory.component.AttunementOverride;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.*;

import java.util.Map;
import java.util.Optional;

public class ClientItemAttunementData {
    private static Map<ResourceLocation, AttunementDataSource> itemAttunementData = null;
    private ClientItemAttunementData() {}

    public static void setAttunementData(Map<ResourceLocation, AttunementDataSource> syncedAttunementData) {
        itemAttunementData = syncedAttunementData;
    }

    public static Optional<AttunementSchema> getClientAttunementSchema(ItemStack stack) {
        Optional<AttunementOverride> override = DataComponentUtil.getAttunementOverride(stack);
        if(override.isPresent()) return Optional.of(override.get());

        Optional<AttunementDataSource> clientSource = getClientAttunementDataSource(stack);
        if(clientSource.isPresent()) return Optional.of(clientSource.get());

        return Optional.empty();
    }

    public static Optional<AttunementSchema> getClientAttunementSchema(AttunedItem attunedItem) {
        if(attunedItem.getAttunementOverrideOpt().isPresent()) return Optional.of(attunedItem.getAttunementOverrideOpt().get());

        Optional<AttunementDataSource> clientSource = getClientAttunementDataSource(attunedItem.getResourceLocation());
        if(clientSource.isPresent()) return Optional.of(clientSource.get());

        return Optional.empty();
    }

    public static boolean isValidAttunementItem(ItemStack stack) {
        if(stack.isEmpty()) return false;
        boolean attunementFlagSet = DataComponentUtil.getAttunementFlag(stack).map(AttunementFlag::isAttunable).orElse(false);
        return attunementFlagSet && getClientAttunementSchema(stack).map(AttunementSchema::isValidSchema).orElse(false);
    }

    public static boolean isUseRestricted(ItemStack stack) {
        return getClientAttunementSchema(stack).filter(AttunementSchema::isValidSchema).map(attunementSchema -> !attunementSchema.useWithoutAttunement()).orElse(false);
    }

    private static Optional<AttunementDataSource> getClientAttunementDataSource(ResourceLocation resourceLocation) {
        if(itemAttunementData == null) return Optional.empty();
        return Optional.ofNullable(itemAttunementData.get(resourceLocation));
    }

    private static Optional<AttunementDataSource> getClientAttunementDataSource(ItemStack stack) {
        if(itemAttunementData == null) return Optional.empty();
        ResourceLocation stackResourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        return getClientAttunementDataSource(stackResourceLocation);
    }

    private static Optional<AttunementDataSource> getClientAttunementDataSource(String resourceLocation) {
        if(itemAttunementData == null) return Optional.empty();
        return getClientAttunementDataSource(ResourceLocation.parse(resourceLocation));
    }
}
