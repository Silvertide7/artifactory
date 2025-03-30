package net.silvertide.artifactory.component;

import java.util.List;

public interface AttunementSchema {
    int attunementSlotsUsed();
    List<AttunementLevel> attunementLevels();
    boolean useWithoutAttunement();

    default boolean isValidSchema() {
        return attunementSlotsUsed() >= 0;
    }
}
