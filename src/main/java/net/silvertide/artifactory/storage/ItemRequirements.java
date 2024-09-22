package net.silvertide.artifactory.storage;

import net.silvertide.artifactory.Artifactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemRequirements {
    List<ItemRequirement> requirements = new ArrayList<>();
    public void addRequirements(List<String> rawRequirements) {
        List<String> itemRequirements = new ArrayList<>(rawRequirements);
        // There are only 3 available slots for items so this will only take
        // the first three in the list.
        for(int i = 0; i < Math.min(itemRequirements.size(), 3); i++) {
            String description = itemRequirements.get(i);
            int quantity = 1;

            // If the requirements has a custom quantity attached to it.
            if(description.contains("#")) {
                String[] itemParts = description.split("#");
                if(itemParts.length > 2) {
                    Artifactory.LOGGER.warn("Artifactory - ItemRequirement not valid - ");
                    continue;
                }

                //TODO Check here if item can stack to more than the stack size it has, cap at stack size
                description = itemParts[0];
                quantity = Integer.parseInt(itemParts[1]);
            }

            requirements.add(new ItemRequirement(description, quantity));
        }
    }

    public String getRequirement(int index) {
        if(index >= requirements.size()) return "";
        return requirements.get(index).description;
    }

    public int getRequirementQuantity(int index) {
        if(index >= requirements.size()) return 0;
        return requirements.get(index).quantity;
    }

    private record ItemRequirement(String description, int quantity){};

}
