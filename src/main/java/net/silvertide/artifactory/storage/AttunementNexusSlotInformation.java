package net.silvertide.artifactory.storage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.GUIUtil;

public record AttunementNexusSlotInformation(int slotsUsed,
                                             int xpConsumed,
                                             int xpThreshold,
                                             int numAttunementLevels,
                                             int levelAchievedByPlayer,
                                             int numSlotsUsedByPlayer,
                                             ItemRequirements itemRequirements) {

    public static AttunementNexusSlotInformation createAttunementNexusSlotInformation(ServerPlayer player, ItemStack stack) {
        if (!AttunementUtil.isValidAttunementItem(stack)) return null;

        return DataPackUtil.getAttunementData(stack).map(itemAttunementData -> {
            // First check if it's already attuned to a player and make sure that is the player requesting this information.
            if (AttunementUtil.isItemAttunedToAPlayer(stack) && !AttunementUtil.isItemAttunedToPlayer(player, stack))
                return null;

            // Get the level of attunement achieved by the player.
            int levelOfAttunementAchievedByPlayer = AttunementUtil.getLevelOfAttunementAchievedByPlayer(player, stack);
            int numLevels = DataPackUtil.getNumAttunementLevels(stack);

            // Set default values. These are used if the player has maxed out the attunement to the item.
            int xpThreshold = -1;
            int xpConsumed = -1;

            // These are possible items required to attune to the item that are consumed.
            ItemRequirements itemRequirements = new ItemRequirements();

            // If the player and item are at max level we only need to send a few of the values.
            // If not lets get all of the relevant data
            if (levelOfAttunementAchievedByPlayer < numLevels) {
                // Get the next levels information.
                AttunementLevel nextAttunementLevel = DataPackUtil.getAttunementLevel(stack, levelOfAttunementAchievedByPlayer + 1);
                if (nextAttunementLevel != null) {
                    xpThreshold = nextAttunementLevel.getRequirements().getXpLevelThreshold() >= 0 ? nextAttunementLevel.getRequirements().getXpLevelThreshold() : Config.XP_LEVELS_TO_ATTUNE_THRESHOLD.get();
                    xpConsumed = nextAttunementLevel.getRequirements().getXpLevelsConsumed() >= 0 ? nextAttunementLevel.getRequirements().getXpLevelsConsumed() : Config.XP_LEVELS_TO_ATTUNE_CONSUMED.get();
                    itemRequirements.addRequirements(nextAttunementLevel.getRequirements().getItems());
                }
            }

            return new AttunementNexusSlotInformation(
                    itemAttunementData.attunementSlotsUsed(),
                    xpConsumed,
                    xpThreshold,
                    numLevels,
                    levelOfAttunementAchievedByPlayer,
                    AttunementUtil.getAttunementSlotsUsed(player),
                    itemRequirements);
        }).orElse(null);
    }

    public boolean isPlayerAtMaxAttuneLevel() {
        return levelAchievedByPlayer() == numAttunementLevels();
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
        if (resourceLocation == null || "".equals(resourceLocation)) return "";
        String itemName = GUIUtil.prettifyName(resourceLocation);
        int quantity = getItemRequirementQuantity(index);
        return quantity + " " + itemName;
    }

    public static void encode(FriendlyByteBuf buf, AttunementNexusSlotInformation slotInformation) {
        buf.writeInt(slotInformation.slotsUsed());
        buf.writeInt(slotInformation.xpConsumed());
        buf.writeInt(slotInformation.xpThreshold());
        buf.writeInt(slotInformation.numAttunementLevels());
        buf.writeInt(slotInformation.levelAchievedByPlayer());
        buf.writeInt(slotInformation.numSlotsUsedByPlayer());
        ItemRequirements.encode(buf, slotInformation.itemRequirements);
    }

    public static AttunementNexusSlotInformation decode(FriendlyByteBuf buf) {
        return new AttunementNexusSlotInformation(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), ItemRequirements.decode(buf));
    }

}
