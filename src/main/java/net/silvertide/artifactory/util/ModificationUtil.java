package net.silvertide.artifactory.util;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.modifications.AttributeModification;
import net.silvertide.artifactory.modifications.AttunementModification;
import net.silvertide.artifactory.modifications.BasicModification;
import net.silvertide.artifactory.modifications.BasicModificationType;

import java.util.Optional;

public final class ModificationUtil {
    private ModificationUtil() {}

    public static void updateItemWithAttunementModifications(ItemStack stack, int level) {
        DataPackUtil.getAttunementData(stack).ifPresent(attunementData -> {
            if (attunementData.attunementLevels().size() >= level - 1) {
                AttunementLevel attunementLevel = attunementData.attunementLevels().get(level - 1);
                if(attunementLevel.getModifications().isEmpty()) return;

                for (String modification : attunementLevel.getModifications()) {
                    applyAttunementModification(stack, modification);
                }
            }
        });
    }

    public static void applyAttunementModification(ItemStack stack, String modificationString) {
        ModificationUtil.createAttunementModification(modificationString).ifPresent(modification -> {
            modification.applyModification(stack);
        });
    }
    public static Optional<AttunementModification> createAttunementModification(String modificationFromAttunementData) {
        if(modificationFromAttunementData.indexOf('/') >= 0) {
            String modificationIdentifier = modificationFromAttunementData.split("/")[0];
            if (modificationIdentifier.equalsIgnoreCase(AttributeModification.ATTRIBUTE_MODIFICATION_TYPE)) {
                return Optional.ofNullable(AttributeModification.fromAttunementDataString(modificationFromAttunementData));
            }
        } else {
            try {
                BasicModificationType modification = BasicModificationType.valueOf(modificationFromAttunementData.toUpperCase());
                return Optional.of(new BasicModification(modification));
            } catch (IllegalArgumentException e) {
                Artifactory.LOGGER.error("Artifactory - Unknown attunement modification " + modificationFromAttunementData);
            }
        }
        return Optional.empty();
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
            for(int i = 0; i < attunementLevelAchieved; i++) {
                AttunementLevel attunementLevel = itemAttunementData.attunementLevels().get(i);
                if(attunementLevel != null) {
                    for(String modificationString : attunementLevel.getModifications()) {
                        if (modification.equals(modificationString)) return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }

}
