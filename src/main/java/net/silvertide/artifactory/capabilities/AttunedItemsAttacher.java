package net.silvertide.artifactory.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.registry.CapabilityRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttunedItemsAttacher {
    private static class WarpCapProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        public static final ResourceLocation IDENTIFIER = new ResourceLocation(Artifactory.MOD_ID, "attuned_items");
        private final AttunedItems backend = new AttunedItemsImpl();
        private final LazyOptional<AttunedItems> optionalData = LazyOptional.of(() -> backend);
        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CapabilityRegistry.ATTUNED_ITEMS_CAPABILITY.orEmpty(cap, this.optionalData);
        }

        void invalidate() {
            this.optionalData.invalidate();
        }

        @Override
        public CompoundTag serializeNBT() {
            return this.backend.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.backend.deserializeNBT(nbt);
        }
    }
    public static void attach(final AttachCapabilitiesEvent<Entity> event) {
        final WarpCapProvider provider = new WarpCapProvider();
        event.addCapability(WarpCapProvider.IDENTIFIER, provider);
    }
}

