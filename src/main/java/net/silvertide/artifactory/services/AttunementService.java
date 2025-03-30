package net.silvertide.artifactory.services;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.AttunementFlag;
import net.silvertide.artifactory.component.AttunementOverride;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.storage.ArtifactorySavedData;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.artifactory.util.*;

public final class AttunementService {
    private AttunementService() {}

    // Attunement Actions
    // This is the main entry point to increase an attunement level.
    public static void increaseLevelOfAttunement(ServerPlayer player, ItemStack stack) {
        int levelOfAttunementAchieved = AttunementUtil.getLevelOfAttunementAchieved(stack);
        boolean successfulAttunementIncrease = false;

        if(levelOfAttunementAchieved == 0 && !AttunementUtil.isItemAttunedToAPlayer(stack)) {
            successfulAttunementIncrease = attuneItemAndPlayer(player, stack);
        } else if(levelOfAttunementAchieved > 0 && AttunementUtil.isItemAttunedToPlayer(player, stack)) {
            successfulAttunementIncrease = DataComponentUtil.getPlayerAttunementData(stack).map(attunementData -> {
                if(attunementData.attunementUUID() != null) {
                    return ArtifactorySavedData.get().increaseLevelOfAttunedItem(player.getUUID(), attunementData.attunementUUID());
                }
                return false;
            }).orElse(false);
        }
        if (successfulAttunementIncrease) {
            ModificationService.applyAttunementModifications(stack);
        }
    }

    // Returns true if the item was successfully attuned, and false if not.
    private static boolean attuneItemAndPlayer(ServerPlayer player, ItemStack stack) {
        if (AttunementUtil.canIncreaseAttunementLevel(player, stack)) {
            AttunedItem attunedItem = AttunedItem.buildAttunedItem(player, stack);
            DataComponentUtil.configurePlayerAttunementData(player, stack, attunedItem);
            ArtifactorySavedData.get().setAttunedItem(player, attunedItem);
            return true;
        }
        return false;
    }

    public static void removeAttunementFromPlayerAndItem(ItemStack stack) {
        DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
            removeUnbreakableIfFromArtifactory(stack, attunementData);
            ArtifactorySavedData.get().removeAttunedItem(attunementData.attunedToUUID(), attunementData.attunementUUID());
        });

        // Clear the attunement data off of the item itself.
        DataComponentUtil.clearPlayerAttunementData(stack);
    }

    public static void clearBrokenAttunements(Player player) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            checkAndUpdateAttunementComponents(player.getInventory().items.get(i));
        }
    }

    // The purpose of this method is to check if the itemstack is attuned to a player
    // but that player is no longer attuned to that item. If so clear the items attunement
    // data, so it can be attuned again. Returns true if a broken attunement was cleaned up.
    // This method also sets the attunement flag information if it's not set.
    public static void checkAndUpdateAttunementComponents(ItemStack stack) {
        if(stack.isEmpty()) return;

        boolean hasNoFlagData = DataComponentUtil.getAttunementFlag(stack).isEmpty();
        DataComponentUtil.getPlayerAttunementData(stack).ifPresentOrElse(playerAttunementData -> {
            if(hasNoFlagData) {
                DataComponentUtil.setAttunementFlag(stack, new AttunementFlag(true, true, 1.0D));
            }

            if (playerAttunementData.attunedToUUID() != null
                    && ArtifactorySavedData.get()
                    .getAttunedItem(playerAttunementData.attunedToUUID(), playerAttunementData.attunementUUID())
                    .isEmpty()) {
                removeUnbreakableIfFromArtifactory(stack, playerAttunementData);
                DataComponentUtil.clearPlayerAttunementData(stack);
            }
        },
        () -> {
            if(hasNoFlagData) {
                AttunementSchemaUtil.getAttunementSchema(stack).filter(AttunementSchema::isValidSchema).ifPresent(attunementSchema -> {
                    if(attunementSchema instanceof AttunementDataSource source) {
                        if(source.chance() == 0.0D) {
                            DataComponentUtil.setAttunementFlag(stack, AttunementFlag.getNonAttunableFlag());
                        } else if (source.chance() >= 1.0D) {
                            DataComponentUtil.setAttunementFlag(stack, AttunementFlag.getAttunableFlag());
                        } else {
                            DataComponentUtil.setAttunementFlag(stack, new AttunementFlag(false, false, Math.max(Math.min(source.chance(), 1.0D), 0.0D)));

                        }
                    } else if(attunementSchema instanceof AttunementOverride) {
                        DataComponentUtil.setAttunementFlag(stack, AttunementFlag.getAttunableFlag());
                    }
                });
            }
        });
    }

    public static void removeUnbreakableIfFromArtifactory(ItemStack stack, PlayerAttunementData playerAttunementData) {
        if (playerAttunementData.isUnbreakable() && stack.get(DataComponents.UNBREAKABLE) != null) {
            DataComponentUtil.removeUnbreakable(stack);
        }
    }

    public static void discoverAttunementItem(ItemStack stack) {

        DataComponentUtil.getAttunementFlag(stack).ifPresent(attunementFlag -> {
            if(!attunementFlag.discovered()) {
                if(Math.random() <= attunementFlag.chance()) {
                    DataComponentUtil.setAttunementFlag(stack, AttunementFlag.getAttunableFlag());
                } else {
                    DataComponentUtil.setAttunementFlag(stack, AttunementFlag.getNonAttunableFlag());
                }
            }
        });
    }

    public static void applyEffectsToPlayer(Player player, ItemStack stack, boolean wearable) {
        if(AttunementUtil.isValidAttunementItem(stack)) {
            if(AttunementUtil.isAttunedToAnotherPlayer(player, stack)) {
                EffectUtil.applyMobEffectInstancesToPlayer(player, ServerConfigs.EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM.get());
            } else if (wearable && !AttunementUtil.isItemAttunedToPlayer(player, stack) && !AttunementSchemaUtil.canUseWithoutAttunement(stack)) {
                EffectUtil.applyMobEffectInstancesToPlayer(player, ServerConfigs.WEAR_EFFECTS_WHEN_USE_RESTRICTED.get());
            }
        }
    }
}
