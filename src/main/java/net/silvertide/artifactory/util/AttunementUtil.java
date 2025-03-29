package net.silvertide.artifactory.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.client.state.ItemRequirements;
import net.silvertide.artifactory.component.AttunementFlag;
import net.silvertide.artifactory.component.AttunementLevel;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.services.PlayerMessenger;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.registry.AttributeRegistry;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;

import java.util.*;

/*
 * This class is used to get information about an attuned item or a player
 */
public final class AttunementUtil {
    private AttunementUtil() {}

    public static int getMaxAttunementSlots(Player player) {
        return (int) player.getAttributeValue(AttributeRegistry.ATTUNEMENT_SLOTS);
    }

    public static int getAttunementSlotsUsed(Player player) {
        Map<UUID, AttunedItem> attunedItems = ArtifactorySavedData.get().getAttunedItems(player.getUUID());
        int numAttunementSlotsUsed = 0;
        for(AttunedItem attunedItem : attunedItems.values()) {
            numAttunementSlotsUsed += AttunementSchemaUtil.getAttunementSchema(attunedItem).map(AttunementSchema::attunementSlotsUsed).orElse(0);
        }
        return numAttunementSlotsUsed;
    }

    public static int getOpenAttunementSlots(Player player) {
        return getMaxAttunementSlots(player) - getAttunementSlotsUsed(player);
    }

    public static int getLevelOfAttunementAchieved(ItemStack stack) {
        return DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> {
            if(attunementData.attunementUUID() != null && attunementData.attunedToUUID() != null) {
                return getLevelOfAttunementAchieved(attunementData.attunedToUUID(), attunementData.attunementUUID());
            }
            return 0;
        }).orElse(0);
    }

    public static int getLevelOfAttunementAchievedByPlayer(ServerPlayer player, ItemStack stack) {
        return DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> {
            if(attunementData.attunementUUID() != null
                    && attunementData.attunedToUUID() != null
                    && player.getUUID().equals(attunementData.attunedToUUID())) {
                return getLevelOfAttunementAchieved(attunementData.attunedToUUID(), attunementData.attunementUUID());
            }
            return 0;
        }).orElse(0);
    }

    public static int getLevelOfAttunementAchieved(UUID playerUUID, UUID itemAttunementUUID) {
        return ArtifactorySavedData.get().getAttunedItem(playerUUID, itemAttunementUUID).map(AttunedItem::getAttunementLevel).orElse(0);
    }

    public static boolean doesPlayerHaveSlotCapacityToAttuneItem(Player player, AttunementSchema attunementSchema) {
        return getOpenAttunementSlots(player) >= attunementSchema.attunementSlotsUsed();
    }

    public static boolean canIncreaseAttunementLevel(Player player, ItemStack stack) {
        if(stack.isEmpty() || !isValidAttunementItem(stack)) return false;
        return AttunementSchemaUtil.getAttunementSchema(stack).map(attunementSchema -> {
            if(isItemAttunedToPlayer(player, stack)) {
                int levelAchieved = getLevelOfAttunementAchieved(stack);
                int maxLevel = AttunementSchemaUtil.getNumAttunementLevels(stack);
                return levelAchieved < maxLevel;
            } else {
                return isAvailableToAttune(stack) && doesPlayerHaveSlotCapacityToAttuneItem(player, attunementSchema);
            }
        }).orElse(false);
    }

    public static boolean isItemAttunedToAPlayer(ItemStack stack) {
        if(stack.isEmpty()) return false;
        return DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> attunementData.attunedToUUID() != null && attunementData.attunementUUID() != null).orElse(false);
    }

    public static boolean isUseRestricted(Player player, ItemStack stack) {
        if(!isValidAttunementItem(stack)) return false;
        return AttunementSchemaUtil.getAttunementSchema(stack).map(attunementSchema -> {
            if(isAttunedToAnotherPlayer(player, stack)) {
                if(player instanceof ServerPlayer serverPlayer) {
                    PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.owned_by_another_player");
                }
                return true;
            } else if(!isItemAttunedToPlayer(player, stack) && !attunementSchema.useWithoutAttunement()) {
                if(player instanceof ServerPlayer serverPlayer) {
                    PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.item_not_usable");
                }
                return true;
            }
            return false;
        }).orElse(false);
    }

    // Soulbound is active if the soulbound flag is set and the player uuid matches the stack attuned to uuid.
    public static boolean isSoulboundActive(ServerPlayer player, ItemStack stack) {
        return isValidAttunementItem(stack) && DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> attunementData.isSoulbound() && attunementData.attunedToUUID().equals(player.getUUID())).orElse(false);
    }

    public static boolean isAttunedToAnotherPlayer(Player player, ItemStack stack) {
        if(stack.isEmpty()) return false;
        return DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> {
            if(attunementData.attunedToUUID() != null) {
                return !player.getUUID().equals(attunementData.attunedToUUID());
            }
            return false;
        }).orElse(false);
    }

    public static boolean arePlayerAndItemAttuned(Player player, ItemStack stack) {
        return isItemAttunedToPlayer(player, stack) && isPlayerAttunedToItem(player, stack);
    }

    public static boolean isItemAttunedToPlayer(Player player, ItemStack stack) {
        if(stack.isEmpty()) return false;
        return DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> {
            if(attunementData.attunedToUUID() == null) return false;
            return attunementData.attunedToUUID().equals(player.getUUID());
        }).orElse(false);
    }

    private static boolean isPlayerAttunedToItem(Player player, ItemStack stack) {
        return DataComponentUtil.getPlayerAttunementData(stack).map(attunementData ->
            ArtifactorySavedData.get().getAttunedItem(player.getUUID(), attunementData.attunementUUID()).isPresent())
                .orElse(false);
    }

    public static boolean doesPlayerHaveAttunedItem(Player player) {
        return !ArtifactorySavedData.get().getAttunedItems(player.getUUID()).isEmpty();
    }

    public static boolean isAvailableToAttune(ItemStack stack) {
        boolean alreadyAttuned = DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> attunementData.attunedToUUID() != null).orElse(false);
        return isValidAttunementItem(stack) && !alreadyAttuned;
    }

    public static boolean isValidAttunementItem(ItemStack stack) {
        if(stack.isEmpty()) return false;
        boolean attunementFlagSet = DataComponentUtil.getAttunementFlag(stack).map(AttunementFlag::isAttunable).orElse(false);
        return attunementFlagSet && AttunementSchemaUtil.getAttunementSchema(stack).map(AttunementSchema::isValidSchema).orElse(false);
    }

    public static String getAttunedItemDisplayName(ItemStack stack) {
        return GUIUtil.prettifyName(DataComponentUtil.getItemDisplayName(stack).orElse(stack.getItem().toString()));
    }

    public static Optional<String> getSavedDataAttunedItemOwnerDisplayName(ItemStack stack) {
        return DataComponentUtil.getPlayerAttunementData(stack).flatMap(attunementData -> ArtifactorySavedData.get().getPlayerName(attunementData.attunedToUUID()));
    }

    public static List<UUID> getPlayerUUIDsWithAttunementToItem(ResourceLocation resourceLocation) {
        List<UUID> results = new ArrayList<>();
        Map<UUID, Map<UUID, AttunedItem>> allAttunedItems = ArtifactorySavedData.get().getAttunedItemsMap();

        for(Map.Entry<UUID, Map<UUID, AttunedItem>> entry : allAttunedItems.entrySet()) {
            for(AttunedItem attunedItem : entry.getValue().values()) {
                if(resourceLocation.toString().equals(attunedItem.getResourceLocation())) {
                    results.add(entry.getKey());
                }
            }
        }
        return results;
    }

    public static AttunementNexusSlotInformation createAttunementNexusSlotInformation(ServerPlayer player, ItemStack stack) {
        if (!isValidAttunementItem(stack)) return null;

        return AttunementSchemaUtil.getAttunementSchema(stack).map(attunementSchema -> {
            // Get the level of attunement achieved by the player.
            int levelOfAttunementAchievedByPlayer = getLevelOfAttunementAchievedByPlayer(player, stack);
            int numLevels = AttunementSchemaUtil.getNumAttunementLevels(stack);

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
                AttunementLevel nextAttunementLevel = AttunementSchemaUtil.getAttunementLevel(stack, levelOfAttunementAchievedByPlayer + 1);
                if (nextAttunementLevel != null) {
                    xpThreshold = nextAttunementLevel.requirements().xpLevelThreshold() >= 0 ? nextAttunementLevel.requirements().xpLevelThreshold() : ServerConfigs.XP_LEVELS_TO_ATTUNE_THRESHOLD.get();
                    xpConsumed = nextAttunementLevel.requirements().xpLevelsConsumed() >= 0 ? nextAttunementLevel.requirements().xpLevelsConsumed() : ServerConfigs.XP_LEVELS_TO_ATTUNE_CONSUMED.get();
                    itemRequirements.addRequirements(nextAttunementLevel.requirements().items());
                }
            }

            return new AttunementNexusSlotInformation(
                    getAttunedItemDisplayName(stack),
                    DataComponentUtil.getPlayerAttunementData(stack)
                            .map(attunementData -> attunementData.attunedToName() != null ? attunementData.attunedToName() : "").orElse(""),
                    isAttunedToAnotherPlayer(player, stack),
                    attunementSchema.attunementSlotsUsed(),
                    xpConsumed,
                    xpThreshold,
                    numLevels,
                    levelOfAttunementAchievedByPlayer,
                    getAttunementSlotsUsed(player),
                    itemRequirements);
        }).orElse(null);
    }
}
