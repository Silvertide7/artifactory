package net.silvertide.artifactory.util;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.modifications.AttributeModification;
import net.silvertide.artifactory.modifications.AttunementModification;
import net.silvertide.artifactory.modifications.BasicModification;
import net.silvertide.artifactory.modifications.BasicModificationType;

public class ModificationUtil {
    private ModificationUtil() {}

    public static void updateItemWithAttunementModifications(ItemStack stack, int level) {
        DataPackUtil.getAttunementData(stack).ifPresent(attunementData -> {
            if (attunementData.attunements().containsKey(String.valueOf(level))) {
                AttunementLevel attunementLevel = attunementData.attunements().get(String.valueOf(level));
                for (String modification : attunementLevel.modifications()) {
                    applyAttunementModification(stack, modification);
                }
            }
        });
    }

    public static void applyAttunementModification(ItemStack stack, String modificationString) {
        AttunementModification attunementModification = ModificationUtil.createAttunementModification(modificationString);
        if (attunementModification != null) {
            attunementModification.applyModification(stack);
        }
    }
    public static AttunementModification createAttunementModification(String modificationFromAttunementData) {
        if(modificationFromAttunementData.indexOf('/') >= 0) {
            String modificationIdentifier = modificationFromAttunementData.split("/")[0];
            if (modificationIdentifier.equalsIgnoreCase(AttributeModification.ATTRIBUTE_MODIFICATION_TYPE)) {
                return AttributeModification.fromAttunementDataString(modificationFromAttunementData);
            }
        } else {
            try {
                BasicModificationType modification = BasicModificationType.valueOf(modificationFromAttunementData.toUpperCase());
                return new BasicModification(modification);
            } catch (IllegalArgumentException e) {
                Artifactory.LOGGER.error("Artifactory - Unknown attunement modification " + modificationFromAttunementData);
            }
        }
        return null;
    }

    public static boolean hasModification(ItemStack stack, String modification) {
        int currentAttunementLevel = AttunementUtil.getLevelOfAttunementAchieved(stack);
        for(int i = 1; i <= currentAttunementLevel; i++){
            if (ModificationUtil.attunementLevelHasModification(stack, i, modification)) {
                return true;
            }
        }
        return false;
    }

    private static boolean attunementLevelHasModification(ItemStack stack, int attunementLevelAchieved, String modification) {
        return DataPackUtil.getAttunementData(stack).map(itemAttunementData -> {
            for(int i = 1; i <= attunementLevelAchieved; i++) {
                AttunementLevel attunementLevel = itemAttunementData.attunements().get(String.valueOf(i));
                if(attunementLevel != null) {
                    for(String modificationString : attunementLevel.modifications()) {
                        if (modification.equals(modificationString)) return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }

}
