package net.silvertide.artifactory.component;

import net.silvertide.artifactory.component.condition.AttunementCondition;

import java.util.List;

public interface AttunementSchema {
    int attunementSlotsUsed();
    List<AttunementLevel> attunementLevels();
    double chance();
    boolean useWithoutAttunement();

    default List<AttunementCondition> attunementConditions() {
        return List.of();
    }

    default boolean isValidSchema() {
        return attunementSlotsUsed() >= 0;
    }
}
