package net.silvertide.artifactory.storage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataPackUtil;

import java.util.Optional;

public record AttunementNexusSlotInformation(int slotsUsed, int xpConsumed, int xpThreshold, int levelAchieved, int maxLevelPossible, String itemRequirementOne, int itemRequirementOneQuantity, String itemRequirementTwo, int itemRequirementTwoQuantity, String itemRequirementThree, int itemRequirementThreeQuantity) {

    public static Optional<AttunementNexusSlotInformation> createAttunementNexusSlotInformation(ServerPlayer player, ItemStack stack) {
        if(!AttunementUtil.isValidAttunementItem(stack)) return Optional.empty();

        return DataPackUtil.getAttunementData(stack).flatMap(itemAttunementData -> {
            // First check if it's already attuned to a player and make sure that is the player requesting this information.
            if(AttunementUtil.isItemAttunedToAPlayer(stack) && !AttunementUtil.isItemAttunedToPlayer(player, stack)) return Optional.empty();

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

            return Optional.of(new AttunementNexusSlotInformation(
                    itemAttunementData.attunementSlotsUsed(),
                    xpConsumed,
                    xpThreshold,
                    currentAttunementLevelOfPlayerToItem,
                    maxLevel,
                    itemRequirements.getItemRequirementOne(),
                    itemRequirements.getItemRequirementOneQuantity(),
                    itemRequirements.getItemRequirementTwo(),
                    itemRequirements.getItemRequirementTwoQuantity(),
                    itemRequirements.getItemRequirementThree(),
                    itemRequirements.getItemRequirementThreeQuantity())
            );
        });
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
}
