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
}
