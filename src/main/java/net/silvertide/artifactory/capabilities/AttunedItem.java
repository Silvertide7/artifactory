package net.silvertide.artifactory.capabilities;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record AttunedItem(UUID id, String name, int slotIndex, int numSlotsTaken) {
    public static final String UUID_NBT_KEY = "uuid";
    public static final String NAME_NBT_KEY = "name";
    public static final String SLOT_INDEX_NBT_KEY = "slotIndex";
    public static final String NUM_SLOTS_TAKEN_NBT_KEY = "uuid";
    public static AttunedItem fromNBT(CompoundTag nbt){
        UUID id = nbt.getUUID(UUID_NBT_KEY);
        String name = nbt.getString(NAME_NBT_KEY);
        int slotIndex = nbt.getInt(SLOT_INDEX_NBT_KEY);
        int numSlotsTaken = nbt.getInt(NUM_SLOTS_TAKEN_NBT_KEY);
        return new AttunedItem(id, name, slotIndex, numSlotsTaken);
    }
}
