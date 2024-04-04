package net.silvertide.artifactory.client.utils;

import net.silvertide.artifactory.storage.AttunedItem;

import java.util.*;

public class ClientAttunedItems {
    private static Map<UUID, AttunedItem> myAttunedItems = new HashMap<>();

    public static void syncAttunedItem(AttunedItem attunedItem) {
        myAttunedItems.put(attunedItem.itemUUID(), attunedItem);
    }

    public static void clearAllAttunedItems() {
        myAttunedItems = new HashMap<>();
    }

//    public static int getNumAttunedItems() {
//        return myAttunedItems.size();
//    }
//
//    public static Optional<AttunedItem> getAttunedItem(UUID playerUUID, UUID attunedItemId) {
//        return Optional.ofNullable(myAttunedItems.get(attunedItemId));
//    }
//
//    public static Map<UUID, AttunedItem> getAttunedItems(UUID playerUUID) {
//        return myAttunedItems;
//    }
//
//    public static List<AttunedItem> getAttunedItemsAsList(UUID playerUUID) {
//        return myAttunedItems.isEmpty() ? new ArrayList<>() : new ArrayList<>(myAttunedItems.values());
//    }
}
