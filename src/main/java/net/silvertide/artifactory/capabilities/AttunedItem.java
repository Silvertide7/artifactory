package net.silvertide.artifactory.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.util.NBTUtil;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.UUID;

public record AttunedItem(UUID id, String resourceLocation, int numSlotsTaken) {
    public static final String ITEM_CAP_UUID_NBT_KEY = "uuid";
    public static final String RESOURCE_LOCATION_NBT_KEY = "resourceLocation";
    public static final String NUM_SLOTS_TAKEN_NBT_KEY = "numSlotsTaken";
    public static AttunedItem fromNBT(CompoundTag nbt){
        UUID id = nbt.getUUID(ITEM_CAP_UUID_NBT_KEY);
        String resourceLocation = nbt.getString(RESOURCE_LOCATION_NBT_KEY);
        int numSlotsTaken = nbt.getInt(NUM_SLOTS_TAKEN_NBT_KEY);
        return new AttunedItem(id, resourceLocation, numSlotsTaken);
    }

    public static AttunedItem buildAttunedItem(ItemStack stack, ItemAttunementData data) {
        UUID itemUUID = NBTUtil.getOrCreateItemAttunementUUID(stack);
        ResourceLocation resourceLocation = ResourceLocationUtil.getResourceLocation(stack);
        int numSlotsTaken = data.attunementSlotsUsed();
        return new AttunedItem(itemUUID, resourceLocation.toString(), numSlotsTaken);
    }
}
