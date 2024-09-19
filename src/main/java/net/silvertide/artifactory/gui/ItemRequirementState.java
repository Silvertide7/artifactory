package net.silvertide.artifactory.gui;

public enum ItemRequirementState {
    NOT_REQUIRED(0),
    EMPTY(1),
    PARTIAL(2),
    FULFILLED(3);

    private final int value;

    ItemRequirementState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
