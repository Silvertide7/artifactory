package net.silvertide.artifactory.component;

import java.util.List;

public interface AttunementSchema {
    int attunementSlotsUsed();
    List<AttunementLevel> attunementLevels();
    boolean useWithoutAttunement();
    boolean unique();

    default boolean isValidSchema() {
        return attunementSlotsUsed() >= 0 && !attunementLevels().isEmpty();
    }
}
