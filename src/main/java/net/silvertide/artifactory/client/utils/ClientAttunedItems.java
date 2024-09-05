package net.silvertide.artifactory.client.utils;

import net.silvertide.artifactory.modifications.AttunementModification;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.ModificationUtil;

import java.util.*;

public class ClientAttunedItems {
    private static Map<UUID, AttunedItem> myAttunedItems = new HashMap<>();
    private static Map<String, String> attunedItemModifications = new HashMap<>();

    public static void setAttunedItem(AttunedItem attunedItem) {
        myAttunedItems.put(attunedItem.itemUUID(), attunedItem);
    }

    public static void setModification(String resourceLocation, String description) {
        attunedItemModifications.put(resourceLocation, description);
    }

    public static List<String> getModifications(String resourceLocation) {
        String modifications = attunedItemModifications.get(resourceLocation);
        return modifications != null ? getModificationDescriptions(modifications) : new ArrayList<>();
    }

    private static List<String> getModificationDescriptions(String modificationSerialization) {
        // These serializations take the form 1#soulbound,invulnerable~2#unbreakable,attribute/...
        // We need to break this apart into usable information by each level.

        ArrayList<String> results = new ArrayList<>();
        // "1#soulbound,invulnerable~2#unbreakable"

        // Break the encoding up by level
        String[] modificationLevelCodes = modificationSerialization.split("~");
        for (String modLevelCode : modificationLevelCodes) {

            // Break it up by the level number and the modifications themselves
            String[] modLevelInformation = modLevelCode.split("#");

            // The first index should be the level (1 or 2 etc).
            // The second index should be the modification codes separated by commas.
            // As such there should only be 2 indices
            if(modLevelInformation.length == 2) {
                StringBuilder levelDescription = new StringBuilder("Level ").append(modLevelInformation[0]).append(": ");

                // Split it apart into individual modification codes
                String[] modificationCodes = modLevelInformation[1].split(",");
                for(int i = 0; i < modificationCodes.length; i++) {
                    String modificationCode = modificationCodes[i];

                    // This code should successfully create a modification. We will then use that modifications toString
                    // to get the relevant information.
                    AttunementModification modification = ModificationUtil.createAttunementModification(modificationCode);
                    if(modification != null) levelDescription.append(modification);
                    if(i != modificationCodes.length - 1) {
                        levelDescription.append(", ");
                    }
                }
                results.add(levelDescription.toString());
            }
        }
        return results;
    }

    public static void clearAllAttunedItems() {
        myAttunedItems = new HashMap<>();
        attunedItemModifications = new HashMap<>();
    }

    public static void removeAttunedItem(UUID itemUUIDToRemove) {
        myAttunedItems.remove(itemUUIDToRemove);
    }

    public static List<AttunedItem> getAttunedItemsAsList() {
        return myAttunedItems.isEmpty() ? new ArrayList<>() : new ArrayList<>(myAttunedItems.values());
    }
}
