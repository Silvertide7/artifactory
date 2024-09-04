package net.silvertide.artifactory.client.utils;

import net.silvertide.artifactory.storage.AttunedItem;

import java.util.*;

public class ClientAttunedItems {
    private static Map<UUID, AttunedItem> myAttunedItems = new HashMap<>();

    public static void setAttunedItem(AttunedItem attunedItem) {
        myAttunedItems.put(attunedItem.itemUUID(), attunedItem);
    }

    public static void clearAllAttunedItems() {
        myAttunedItems = new HashMap<>();
    }

    public static void removeAttunedItem(UUID itemUUIDToRemove) {
        myAttunedItems.remove(itemUUIDToRemove);
    }

    public static List<AttunedItem> getAttunedItemsAsList(UUID playerUUID) {
        return myAttunedItems.isEmpty() ? new ArrayList<>() : new ArrayList<>(myAttunedItems.values());
    }
}
