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

    @Override
    public Optional<AttunedItem> getAttunedItem(UUID attunedItemId) {
        if(this.attunedItems.containsKey(attunedItemId)) {
            return Optional.of(this.attunedItems.get(attunedItemId));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<AttunedItem>> getAttunedItemsAsList() {
        if(!this.attunedItems.isEmpty()) {
            List<AttunedItem> items = new ArrayList<>(this.attunedItems.values());
            return Optional.of(items);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Map<UUID, AttunedItem> getAttunedItemsMap() {
        return this.attunedItems;
    }

    @Override
    public void setAttunedItems(Map<UUID, AttunedItem> attunedItems) {
        this.attunedItems = attunedItems;
    }

    @Override
    public void addAttunedItem(AttunedItem itemToAttune) {
        attunedItems.put(itemToAttune.id(), itemToAttune);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        //TODO: REdo this with a map . entry set

        for (Map.Entry<UUID, AttunedItem> attunedItem : attunedItems.entrySet()) {
            CompoundTag attunementInfo = new CompoundTag();

            attunementInfo.putUUID(AttunedItems.ITEM_CAP_UUID_NBT_KEY, attunedItem.getValue().id());
            attunementInfo.putString(AttunedItems.RESOURCE_LOCATION_NBT_KEY, attunedItem.getValue().resourceLocation());
            attunementInfo.putInt(AttunedItems.NUM_SLOTS_TAKEN_NBT_KEY, attunedItem.getValue().numSlotsTaken());

            nbt.put(attunedItem.getValue().id().toString(), attunementInfo);
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
        setAttunedItems(attunedItems);
    }
}
