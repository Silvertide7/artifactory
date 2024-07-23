package net.silvertide.artifactory.client.utils;

import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.*;

public class ClientAttunedItems {
    private static Map<UUID, AttunedItem> myAttunedItems = new HashMap<>();

    public static void setAttunedItem(AttunedItem attunedItem) {
        myAttunedItems.put(attunedItem.itemUUID(), attunedItem);
        onChange();
    }

    private static void onChange() {
        Artifactory.LOGGER.info("CLIENT DATA -------");
        for(Map.Entry<UUID, AttunedItem> attunedItem : myAttunedItems.entrySet()) {
            Artifactory.LOGGER.info("Attuned item : " + attunedItem);
        }
    }

    public static void clearAllAttunedItems() {
        myAttunedItems = new HashMap<>();
        onChange();
    }

    public static void removeAttunedItem(UUID itemUUIDToRemove) {
        myAttunedItems.remove(itemUUIDToRemove);
        onChange();
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

    public static List<AttunedItem> getAttunedItemsAsList(UUID playerUUID) {
        return myAttunedItems.isEmpty() ? new ArrayList<>() : new ArrayList<>(myAttunedItems.values());
    }
}
