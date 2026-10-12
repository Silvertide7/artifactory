package net.silvertide.artifactory.storage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record AttunementNexusSlotInformation(String itemName, String attunedToName, boolean attunedByAnotherPlayer, int slotsUsed, int xpConsumed, int xpThreshold, int numAttunementLevels, int levelAchievedByPlayer, int numSlotsUsedByPlayer, ItemRequirements itemRequirements, List<Component> unmetAttunementConditions) {
    public boolean isPlayerAtMaxAttuneLevel() {
        if(numAttunementLevels() == 0) {
            return levelAchievedByPlayer() >= 1;
        }
        return levelAchievedByPlayer() >= numAttunementLevels();
    }

    public ItemStack getItemRequirement(int index) {
        return itemRequirements.getItemRequired(index);
    }

    public boolean hasItemRequirement(int index) {
        ItemStack stack = getItemRequirement(index);
        return stack != null && !stack.isEmpty();
    }

    public Component getItemRequirementText(int index) {
        ItemStack stack = getItemRequirement(index);
        if (stack.isEmpty()) return Component.empty();
        return Component.translatable("screen.tooltip.artifactory.attunement.requires_item_count", stack.getCount())
                .append(stack.getHoverName());
    }
}
