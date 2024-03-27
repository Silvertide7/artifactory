package net.silvertide.artifactory.capabilities;

import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class AttunedItemsImpl implements AttunedItems {

    private Map<UUID, AttunedItem> attunedItems;

    public AttunedItemsImpl() {
        attunedItems = new HashMap<>();
    }

    @Override
    public int getNumAttunedItems() {
        return attunedItems.size();
    }

    private void setAttunedItems(Map<UUID, AttunedItem> attunedItems) {
        this.attunedItems = attunedItems;
    }

    @Override
    public void attuneItem(AttunedItem itemToAttune) {
        attunedItems.put(itemToAttune.id(), itemToAttune);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        for(int i = 0; i < attunedItems.size(); i++) {
            AttunedItem attunedItem = attunedItems.get(i);
            CompoundTag itemTag = new CompoundTag();

            itemTag.putUUID(AttunedItem.UUID_NBT_KEY, attunedItem.id());
            itemTag.putString(AttunedItem.NAME_NBT_KEY, attunedItem.name());
            itemTag.putInt(AttunedItem.SLOT_INDEX_NBT_KEY, attunedItem.slotIndex());
            itemTag.putInt(AttunedItem.NUM_SLOTS_TAKEN_NBT_KEY, attunedItem.numSlotsTaken());

            nbt.put(attunedItem.id().toString(), itemTag);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        Map<UUID, AttunedItem> attunedItems = new HashMap<>();
        if(nbt.getAllKeys().size() > 0) {
            nbt.getAllKeys().forEach((key) -> {
                AttunedItem itemFromNBT = AttunedItem.fromNBT(nbt.getCompound(key));
                attunedItems.put(itemFromNBT.id(), itemFromNBT);
            });
        }
        setAttunedItems(attunedItems);;
    }
}
