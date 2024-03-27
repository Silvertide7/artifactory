package net.silvertide.artifactory.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.silvertide.artifactory.capabilities.AttunedItems;

import static net.silvertide.artifactory.registry.CapabilityRegistry.ATTUNED_ITEMS_CAPABILITY;

public final class CapabilityUtil {
    private CapabilityUtil() {}

    public static LazyOptional<AttunedItems> getAttunedItems(final LivingEntity entity) {
        if (entity == null) return LazyOptional.empty();
        return entity.getCapability(ATTUNED_ITEMS_CAPABILITY);
    }

}
