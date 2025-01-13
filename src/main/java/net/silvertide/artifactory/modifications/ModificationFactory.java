package net.silvertide.artifactory.modifications;

import net.silvertide.artifactory.Artifactory;

import java.util.Optional;

public class ModificationFactory {
    public static Optional<AttunementModification> createAttunementModification(String modificationCode) {
        if(modificationCode.indexOf('/') >= 0) {
            String modificationIdentifier = modificationCode.split("/")[0];
            if (modificationIdentifier.equalsIgnoreCase("attribute")) {
                return Optional.ofNullable(AttributeModification.fromAttunementDataString(modificationCode));
            }
        } else {
            try {
                BasicModificationType modification = BasicModificationType.valueOf(modificationCode.toUpperCase());
                return Optional.of(new BasicModification(modification));
            } catch (IllegalArgumentException e) {
                Artifactory.LOGGER.error("Artifactory - Unknown attunement modification " + modificationCode);
            }
        }
        return Optional.empty();
    }
}
