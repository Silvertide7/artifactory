package net.silvertide.artifactory.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface AttunedItems extends INBTSerializable<CompoundTag> {
    public static final String ITEM_CAP_UUID_NBT_KEY = "itemAttunementUUID";
    public static final String RESOURCE_LOCATION_NBT_KEY = "itemResourceLocation";
    public static final String NUM_SLOTS_TAKEN_NBT_KEY = "numSlotsTaken";
    int getNumAttunedItems();
    void addAttunedItem(AttunedItem itemToAttune);
    Optional<AttunedItem> getAttunedItem(UUID attunedItemId);
    Optional<List<AttunedItem>> getAttunedItemsAsList();
    Map<UUID, AttunedItem> getAttunedItemsMap();
    void setAttunedItems(Map<UUID, AttunedItem> attunedItems);
}
