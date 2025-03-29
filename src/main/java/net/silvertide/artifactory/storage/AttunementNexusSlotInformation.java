package net.silvertide.artifactory.storage;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.client.state.ItemRequirements;

public record AttunementNexusSlotInformation(String itemName, String attunedToName, boolean attunedByAnotherPlayer, int slotsUsed, int xpConsumed, int xpThreshold, int numAttunementLevels, int levelAchievedByPlayer, int numSlotsUsedByPlayer, ItemRequirements itemRequirements) {
    public boolean isPlayerAtMaxAttuneLevel() {
        return levelAchievedByPlayer() >= numAttunementLevels();
    }

    public ItemStack getItemRequirement(int index) {
        return itemRequirements.getItemRequired(index);
    }

    public boolean matchesItemRequirement(int index, ItemStack stack) {
        ItemStack requiredStack = getItemRequirement(index);
        if (requiredStack.isEmpty() || stack.isEmpty()) {
            return requiredStack.isEmpty() && stack.isEmpty();
        }
        return requiredStack.is(stack.getItem()) && requiredStack.getCount() == stack.getCount();
    }

    public boolean hasItemRequirement(int index) {
        ItemStack stack = getItemRequirement(index);
        return stack != null && !stack.isEmpty();
    }

    public String getItemRequirementText(int index) {
        ItemStack stack = getItemRequirement(index);
        if (stack.isEmpty()) return "";
        return stack.getCount() + " " + stack.getDisplayName();
    }
}
