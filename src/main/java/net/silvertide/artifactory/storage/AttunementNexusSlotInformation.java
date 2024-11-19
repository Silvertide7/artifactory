package net.silvertide.artifactory.storage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.util.*;

import java.util.List;
import java.util.UUID;

public record AttunementNexusSlotInformation(String itemName, String attunedToName, boolean attunedByAnotherPlayer, int slotsUsed, String uniqueStatus, int xpConsumed, int xpThreshold, int numAttunementLevels, int levelAchievedByPlayer, int numSlotsUsedByPlayer, ItemRequirements itemRequirements) {

    public static AttunementNexusSlotInformation createAttunementNexusSlotInformation(ServerPlayer player, ItemStack stack) {
        if (!AttunementUtil.isValidAttunementItem(stack)) return null;

        return DataPackUtil.getAttunementData(stack).map(itemAttunementData -> {
            // Get the level of attunement achieved by the player.
            int levelOfAttunementAchievedByPlayer = AttunementUtil.getLevelOfAttunementAchievedByPlayer(player, stack);
            int numLevels = DataPackUtil.getNumAttunementLevels(stack);

            // Set default values. These are used if the player has maxed out the attunement to the item.
            int xpThreshold = -1;
            int xpConsumed = -1;
            String uniqueStatus = "";

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

            // If the player isn't attuned to the item we need to first check if its a unique item
            // and the item type has no other attuned owners. If there is then it can't be attuned by
            // the player.
            if(itemAttunementData.unique() && levelOfAttunementAchievedByPlayer == 0) {
                List<UUID> ownerUUIDs = AttunementUtil.getPlayerUUIDsWithAttunementToItem(ResourceLocationUtil.getResourceLocation(stack));
                if(!ownerUUIDs.isEmpty()) {
                    if(ownerUUIDs.contains(player.getUUID())) {
                        uniqueStatus = UniqueStatus.ALREADY_ATTUNED_BY_THIS_PLAYER;
                    } else {
                        uniqueStatus = UniqueStatus.ATTUNED_BY_ANOTHER_PLAYER;
                    }
                } else {
                    if (AttunementUtil.isPlayerAtUniqueAttunementLimit(player.getUUID())) {
                        uniqueStatus = UniqueStatus.REACHED_UNIQUE_CAPACITY;
                    }
                }
            }

            return new AttunementNexusSlotInformation(
                    AttunementUtil.getAttunedItemDisplayName(stack),
                    StackNBTUtil.getAttunedToName(stack).orElse(""),
                    AttunementUtil.isAttunedToAnotherPlayer(player, stack),
                    itemAttunementData.attunementSlotsUsed(),
                    uniqueStatus,
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
        buf.writeUtf(slotInformation.itemName());
        buf.writeUtf(slotInformation.attunedToName());
        buf.writeBoolean(slotInformation.attunedByAnotherPlayer);
        buf.writeInt(slotInformation.slotsUsed());
        buf.writeUtf(slotInformation.uniqueStatus());
        buf.writeInt(slotInformation.xpConsumed());
        buf.writeInt(slotInformation.xpThreshold());
        buf.writeInt(slotInformation.numAttunementLevels());
        buf.writeInt(slotInformation.levelAchievedByPlayer());
        buf.writeInt(slotInformation.numSlotsUsedByPlayer());
        ItemRequirements.encode(buf, slotInformation.itemRequirements);
    }

    public static AttunementNexusSlotInformation decode(FriendlyByteBuf buf) {
        return new AttunementNexusSlotInformation(buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readInt(), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), ItemRequirements.decode(buf));
    }

}
