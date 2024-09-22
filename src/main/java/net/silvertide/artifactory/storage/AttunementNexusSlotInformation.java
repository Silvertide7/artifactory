package net.silvertide.artifactory.storage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.GUIUtil;

import java.util.Optional;

public record AttunementNexusSlotInformation(int slotsUsed, int xpConsumed, int xpThreshold, int levelAchieved, int maxLevelPossible, String itemRequirementOne, int itemRequirementOneQuantity, String itemRequirementTwo, int itemRequirementTwoQuantity, String itemRequirementThree, int itemRequirementThreeQuantity) {

    public static AttunementNexusSlotInformation createAttunementNexusSlotInformation(ServerPlayer player, ItemStack stack) {
        if(!AttunementUtil.isValidAttunementItem(stack)) return null;

        return DataPackUtil.getAttunementData(stack).map(itemAttunementData -> {
            // First check if it's already attuned to a player and make sure that is the player requesting this information.
            if(AttunementUtil.isItemAttunedToAPlayer(stack) && !AttunementUtil.isItemAttunedToPlayer(player, stack)) return null;

            // Get the level of attunement achieved by the player.
            int currentAttunementLevelOfPlayerToItem = AttunementUtil.getLevelOfAttunementAchievedByPlayer(player, stack);
            int maxLevel = DataPackUtil.getMaxLevelOfAttunementPossible(stack);

            // Set default values. These are used if the player has maxed out the attunement to the item.
            int xpThreshold = -1;
            int xpConsumed = -1;

            // These are possible items required to attune to the item that are consumed.
            ItemRequirements itemRequirements = new ItemRequirements();

            // If the player and item are at max level we only need to send a few of the values.
            // If not lets get all of the relevant data
            if(currentAttunementLevelOfPlayerToItem < maxLevel) {
                // Get the next levels information.
                Optional<AttunementLevel> nextAttunementLevel = DataPackUtil.getAttunementLevel(stack, currentAttunementLevelOfPlayerToItem + 1);
                if(nextAttunementLevel.isPresent()) {
                    xpThreshold = nextAttunementLevel.get().requirements().xpLevelThreshold() >= 0 ? nextAttunementLevel.get().requirements().xpLevelThreshold() : Config.XP_LEVELS_TO_ATTUNE_THRESHOLD.get();
                    xpConsumed = nextAttunementLevel.get().requirements().xpLevelsConsumed() >= 0 ? nextAttunementLevel.get().requirements().xpLevelsConsumed() : Config.XP_LEVELS_TO_ATTUNE_CONSUMED.get();
                    itemRequirements.addRequirements(nextAttunementLevel.get().requirements().items());
                }
            }

            return new AttunementNexusSlotInformation(
                    itemAttunementData.attunementSlotsUsed(),
                    xpConsumed,
                    xpThreshold,
                    currentAttunementLevelOfPlayerToItem,
                    maxLevel,
                    itemRequirements.getRequirement(0),
                    itemRequirements.getRequirementQuantity(0),
                    itemRequirements.getRequirement(1),
                    itemRequirements.getRequirementQuantity(1),
                    itemRequirements.getRequirement(2),
                    itemRequirements.getRequirementQuantity(2));
        }).orElse(null);
    }

    public static void encode(FriendlyByteBuf buf, AttunementNexusSlotInformation slotInformation) {
        buf.writeInt(slotInformation.slotsUsed());
        buf.writeInt(slotInformation.xpConsumed());
        buf.writeInt(slotInformation.xpThreshold());
        buf.writeInt(slotInformation.levelAchieved());
        buf.writeInt(slotInformation.maxLevelPossible());
        buf.writeUtf(slotInformation.itemRequirementOne());
        buf.writeInt(slotInformation.itemRequirementOneQuantity());
        buf.writeUtf(slotInformation.itemRequirementTwo());
        buf.writeInt(slotInformation.itemRequirementTwoQuantity());
        buf.writeUtf(slotInformation.itemRequirementThree());
        buf.writeInt(slotInformation.itemRequirementThreeQuantity());
    }

    public static AttunementNexusSlotInformation decode(FriendlyByteBuf buf) {
        int slotsUsed = buf.readInt();
        int xpConsumed = buf.readInt();
        int xpThreshold = buf.readInt();
        int levelAchieved = buf.readInt();
        int maxLevelPossible = buf.readInt();
        String itemRequirementOne = buf.readUtf();
        int itemRequirementOneQuantity = buf.readInt();
        String itemRequirementTwo = buf.readUtf();
        int itemRequirementTwoQuantity = buf.readInt();
        String itemRequirementThree = buf.readUtf();
        int itemRequirementThreeQuantity = buf.readInt();
        return new AttunementNexusSlotInformation(slotsUsed, xpConsumed, xpThreshold, levelAchieved, maxLevelPossible, itemRequirementOne, itemRequirementOneQuantity, itemRequirementTwo, itemRequirementTwoQuantity, itemRequirementThree, itemRequirementThreeQuantity);
    }

    public String getItemRequirement(int index) {
        return switch (index) {
            case 0 -> this.itemRequirementOne();
            case 1 -> this.itemRequirementTwo();
            case 2 -> this.itemRequirementThree();
            default -> "";
        };
    }

    public int getItemRequirementQuantity(int index) {
        return switch (index) {
            case 0 -> this.itemRequirementOneQuantity();
            case 1 -> this.itemRequirementTwoQuantity();
            case 2 -> this.itemRequirementThreeQuantity();
            default -> 0;
        };
    }

    public boolean hasItemRequirement(int index) {
        return getItemRequirementQuantity(index) > 0 && !"".equals(getItemRequirement(index));
    }

    public String getItemRequirementText(int index) {
        String resourceLocation = getItemRequirement(index);
        if(resourceLocation == null || "".equals(resourceLocation)) return "";
        String itemName = GUIUtil.prettifyName(resourceLocation);
        int quantity = getItemRequirementQuantity(index);
        return quantity + "x " + itemName;
    }

    public boolean meetsItemRequirement(int index, String itemResourceLocation, int itemQuantity) {
        if(getItemRequirementQuantity(index) == 0 || "".equals(getItemRequirement(index))) return true;
        return getItemRequirement(index).equals(itemResourceLocation) && getItemRequirementQuantity(index) == itemQuantity;
    }
}
