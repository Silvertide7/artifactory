package net.silvertide.artifactory.client.state;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.ResourceLocationUtil;

import java.util.ArrayList;
import java.util.List;

public class ItemRequirements {
    List<ItemStack> requirements = new ArrayList<>();
    public void addRequirements(List<String> rawRequirements) {
        List<String> itemRequirements = new ArrayList<>(rawRequirements);
        // There are only 3 available slots for items so this will only take
        // the first three in the list.
        for(int i = 0; i < Math.min(itemRequirements.size(), 3); i++) {
            addItemRequirement(itemRequirements.get(i));
        }
    }

    public void addItemRequirement(String itemRequirementString) {
        ItemStack stack = parseItemStack(itemRequirementString);
        if(stack != null && !stack.isEmpty()) {
            this.requirements.add(stack);
        } else {
            Artifactory.LOGGER.warn("Artifactory - ItemRequirement not valid, invalid item - " + itemRequirementString);
        }
    }

    public static ItemStack parseItemStack(String itemRequirementString) {
        String pathResult = itemRequirementString;
        int quantity = 1;

        // If the requirements has a custom quantity attached to it. modid:item_name#quantity
        if(pathResult.contains("#")) {
            String[] itemParts = pathResult.split("#");
            if(itemParts.length > 2) {
                return null;
            }
            pathResult = itemParts[0];
            quantity = Integer.parseInt(itemParts[1]);
        }

        ItemStack stack = ResourceLocationUtil.getItemStackFromResourceLocation(pathResult);
        if(stack != null && !stack.isEmpty()) {
            stack.setCount(quantity);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getItemRequired(int index) {
        if(index >= requirements.size()) return ItemStack.EMPTY;
        return requirements.get(index);
    }
}
