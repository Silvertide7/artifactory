package net.silvertide.artifactory.modifications;

import net.silvertide.artifactory.Artifactory;

import java.util.Optional;

public class AttunementModificationFactory {
    private AttunementModificationFactory() {}

    public static Optional<AttunementModification> createAttunementModification(String modificationFromAttunementData) {
        if(modificationFromAttunementData.indexOf('/') >= 0) {
            String modificationIdentifier = modificationFromAttunementData.split("/")[0];
            if (modificationIdentifier.toUpperCase().equals(AttributeModification.ATTRIBUTE_MODIFICATION_TYPE)) {
                AttributeModification attributeModification = AttributeModification.fromAttunementDataString(modificationFromAttunementData);
                if (attributeModification != null) {
                    return Optional.of(attributeModification);
                }
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
