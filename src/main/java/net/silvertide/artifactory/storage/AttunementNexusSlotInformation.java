package net.silvertide.artifactory.storage;

import net.silvertide.artifactory.client.state.ItemRequirements;
import net.silvertide.artifactory.util.GUIUtil;

public record AttunementNexusSlotInformation(String itemName, String attunedToName, boolean attunedByAnotherPlayer, int slotsUsed, int xpConsumed, int xpThreshold, int numAttunementLevels, int levelAchievedByPlayer, int numSlotsUsedByPlayer, ItemRequirements itemRequirements) {
    public boolean isPlayerAtMaxAttuneLevel() {
        return levelAchievedByPlayer() >= numAttunementLevels();
    }

    public String getItemRequirement(int index) {
        return itemRequirements.getItemResourceLocation(index);
    }

    public int getItemRequirementQuantity(int index) {
        return itemRequirements.getItemQuantity(index);
    }

    public boolean hasItemRequirement(int index) {
        return getItemRequirementQuantity(index) > 0 && !"".equals(getItemRequirement(index));
    }

    public String getItemRequirementText(int index) {
        String resourceLocation = getItemRequirement(index);
        if (resourceLocation == null || resourceLocation.isEmpty()) return "";
        String itemName = GUIUtil.prettifyName(resourceLocation);
        int quantity = getItemRequirementQuantity(index);
        return quantity + " " + itemName;
    }
}
