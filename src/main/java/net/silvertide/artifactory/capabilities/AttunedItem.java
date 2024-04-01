package net.silvertide.artifactory.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.util.NBTUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.Optional;
import java.util.UUID;

public record AttunedItem(UUID id, String resourceLocation, int numSlotsTaken) {

    public static AttunedItem fromNBT(CompoundTag nbt){
        UUID id = nbt.getUUID(AttunedItems.ITEM_CAP_UUID_NBT_KEY);
        String resourceLocation = nbt.getString(AttunedItems.RESOURCE_LOCATION_NBT_KEY);
        int numSlotsTaken = nbt.getInt(AttunedItems.NUM_SLOTS_TAKEN_NBT_KEY);
        return new AttunedItem(id, resourceLocation, numSlotsTaken);
    }

    public static Optional<AttunedItem> buildAttunedItem(ItemStack stack, ItemAttunementData data) {
        return NBTUtil.getItemAttunementUUID(stack).flatMap(itemUUID -> {
            ResourceLocation resourceLocation = ResourceLocationUtil.getResourceLocation(stack);
            int numSlotsTaken = data.getAttunementSlotsUsed();
            return Optional.of(new AttunedItem(itemUUID, resourceLocation.toString(), numSlotsTaken));
        });
    }
}
