package net.silvertide.artifactory.client.state;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.AttunementFlag;
import net.silvertide.artifactory.component.AttunementLevel;
import net.silvertide.artifactory.component.AttunementOverride;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.storage.AttunementNexusSlotInformation;
import net.silvertide.artifactory.util.DataComponentUtil;
import net.silvertide.artifactory.util.GUIUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ClientAttunementUtil {
    private ClientAttunementUtil() {}

    public static Optional<AttunementSchema> getClientAttunementSchema(ItemStack stack) {
        AttunementOverride override = DataComponentUtil.getAttunementOverride(stack);
        if(override.isValidSchema()) return Optional.of(override);

        Optional<AttunementDataSource> clientSource = ClientAttunementDataSource.getClientAttunementDataSource(stack);
        if(clientSource.isPresent() && clientSource.get().isValidSchema()) return Optional.of(clientSource.get());

        return Optional.empty();
    }

    public static Optional<AttunementSchema> getClientAttunementSchema(AttunedItem attunedItem) {
        if(attunedItem.getAttunementOverrideOpt().isPresent()) return Optional.of(attunedItem.getAttunementOverrideOpt().get());

        Optional<AttunementDataSource> clientSource = ClientAttunementDataSource.getClientAttunementDataSource(attunedItem.getResourceLocation());
        if(clientSource.isPresent() && clientSource.get().isValidSchema()) return Optional.of(clientSource.get());

        return Optional.empty();
    }

    public static boolean isValidAttunementItem(ItemStack stack) {
        if(stack.isEmpty()) return false;
        boolean attunementFlagSet = DataComponentUtil.getAttunementFlag(stack).map(AttunementFlag::isAttunable).orElse(false);
        return attunementFlagSet && getClientAttunementSchema(stack).map(AttunementSchema::isValidSchema).orElse(false);
    }

    public static boolean isUseRestricted(Player player, ItemStack stack) {
        return getClientAttunementSchema(stack)
                .filter(AttunementSchema::isValidSchema)
                .map(attunementSchema ->
                        DataComponentUtil.getPlayerAttunementData(stack)
                                .map(playerAttunementData -> !player.getUUID().equals(playerAttunementData.attunedToUUID()))
                        .orElse(!attunementSchema.useWithoutAttunement()))
                .orElse(false);
    }
    public static int getLevelOfAttunementAchievedByPlayer(LocalPlayer player, ItemStack stack) {
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
        return ClientAttunedItems.getAttunedItem(playerUUID, itemAttunementUUID).map(AttunedItem::getAttunementLevel).orElse(0);
    }

    public static String getAttunedItemDisplayName(ItemStack stack) {
        return GUIUtil.prettifyName(DataComponentUtil.getItemDisplayName(stack).orElse(stack.getItem().toString()));
    }

    public static boolean isAttunedToAnotherPlayer(LocalPlayer player, ItemStack stack) {
        if(stack.isEmpty()) return false;
        return DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> {
            if(attunementData.attunedToUUID() != null) {
                return !player.getUUID().equals(attunementData.attunedToUUID());
            }
            return false;
        }).orElse(false);
    }

    public static int getAttunementSlotsUsed() {
        Map<UUID, AttunedItem> attunedItems = ClientAttunedItems.getMyAttunedItems();
        int numAttunementSlotsUsed = 0;
        for(AttunedItem attunedItem : attunedItems.values()) {
            numAttunementSlotsUsed += ClientAttunementUtil.getClientAttunementSchema(attunedItem).map(AttunementSchema::attunementSlotsUsed).orElse(0);
        }
        return numAttunementSlotsUsed;
    }

    public static int getNumAttunementLevels(ItemStack stack) {
        return getClientAttunementSchema(stack).map(attunementData -> attunementData.attunementLevels().size()).orElse(0);
    }

    public static AttunementLevel getAttunementLevel(ItemStack stack, int level) {
        return getClientAttunementSchema(stack).map(attunementData -> {
            if(attunementData.attunementLevels().isEmpty()) return null;
            return attunementData.attunementLevels().get(level - 1);
        }).orElse(null);
    }

    public static AttunementNexusSlotInformation createAttunementNexusSlotInformation(LocalPlayer localPlayer, ItemStack stack) {
        if (!ClientAttunementUtil.isValidAttunementItem(stack)) return null;

        return ClientAttunementUtil.getClientAttunementSchema(stack).map(attunementSchema -> {
            // Get the level of attunement achieved by the player.
            int levelOfAttunementAchievedByPlayer = ClientAttunementUtil.getLevelOfAttunementAchievedByPlayer(localPlayer, stack);
            int numLevels = getNumAttunementLevels(stack);

            // Set default values. These are used if the player has maxed out the attunement to the item.
            int xpThreshold = -1;
            int xpConsumed = -1;

            // These are possible items required to attune to the item that are consumed.
            ItemRequirements itemRequirements = new ItemRequirements();

            // If the player and item are at max level we only need to send a few of the values.
            // If not lets get all the relevant data
            if(numLevels == 0 && levelOfAttunementAchievedByPlayer == 0) {
                xpThreshold = ClientSyncedConfig.getXpAttuneThreshold();
                xpConsumed = ClientSyncedConfig.getXpAttuneConsumed();
            }
            else if (levelOfAttunementAchievedByPlayer < numLevels) {
                // Get the next levels information.
                AttunementLevel nextAttunementLevel = getAttunementLevel(stack, levelOfAttunementAchievedByPlayer + 1);
                if (nextAttunementLevel != null) {
                    xpThreshold = nextAttunementLevel.requirements().xpLevelThreshold() >= 0 ? nextAttunementLevel.requirements().xpLevelThreshold() : ClientSyncedConfig.getXpAttuneThreshold();
                    xpConsumed = nextAttunementLevel.requirements().xpLevelsConsumed() >= 0 ? nextAttunementLevel.requirements().xpLevelsConsumed() : ClientSyncedConfig.getXpAttuneConsumed();
                    itemRequirements.addRequirements(nextAttunementLevel.requirements().items());
                }
            }

            // If the player isn't attuned to the item we need to first check if its a unique item
            // and the item type has no other attuned owners. If there is then it can't be attuned by
            // the player.
            return new AttunementNexusSlotInformation(
                    ClientAttunementUtil.getAttunedItemDisplayName(stack),
                    DataComponentUtil.getPlayerAttunementData(stack)
                            .map(attunementData -> attunementData.attunedToName() != null ? attunementData.attunedToName() : "").orElse(""),
                    ClientAttunementUtil.isAttunedToAnotherPlayer(localPlayer, stack),
                    attunementSchema.attunementSlotsUsed(),
                    xpConsumed,
                    xpThreshold,
                    numLevels,
                    levelOfAttunementAchievedByPlayer,
                    ClientAttunementUtil.getAttunementSlotsUsed(),
                    itemRequirements);
        }).orElse(null);
    }
}
