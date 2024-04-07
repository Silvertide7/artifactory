package net.silvertide.artifactory.util;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.modifications.AttributeModification;
import net.silvertide.artifactory.modifications.AttunementModification;
import net.silvertide.artifactory.modifications.BasicModification;
import net.silvertide.artifactory.modifications.BasicModificationType;

import java.util.Optional;

public class AttunementModificationUtil {
    private AttunementModificationUtil() {}

    public static Optional<AttunementModification> createAttunementModification(String modificationFromAttunementData) {
        if(modificationFromAttunementData.indexOf('/') >= 0) {
            String modificationIdentifier = modificationFromAttunementData.split("/")[0];
            if (modificationIdentifier.toUpperCase().equals(AttributeModification.ATTRIBUTE_MODIFICATION_TYPE)) {
                return AttributeModification.fromAttunementDataString(modificationFromAttunementData);
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

    public static boolean hasModification(ItemStack stack, int attunementLevelAchieved, String modification) {
        return ArtifactUtil.getAttunementData(stack).map(itemAttunementData -> {
            for(int i = 1; i <= attunementLevelAchieved; i++) {
                AttunementLevel attunementLevel = itemAttunementData.attunements().get(String.valueOf(i));
                if(attunementLevel != null) {
                    for(String modificationString : attunementLevel.modifications()) {
                        if (modificationString.equals(modification)) return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }

}
