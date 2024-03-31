package net.silvertide.artifactory.registry;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.silvertide.artifactory.capabilities.AttunedItems;

public class CapabilityRegistry {
    public static final Capability<AttunedItems> ATTUNED_ITEMS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
}
