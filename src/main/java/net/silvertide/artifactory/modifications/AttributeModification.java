package net.silvertide.artifactory.modifications;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;

public class AttributeModification implements AttunementModification {
    public static final String ATTRIBUTE_MODIFICATION_TYPE = "ADD_ATTRIBUTE";
    private String attribute;
    private AttributeModifier.Operation  operation;
    private double value;

    private AttributeModification(String attribute, AttributeModifier.Operation operation, double value) {
        this.attribute = attribute;
        this.operation = operation;
        this.value = value;
    }
    public static AttributeModification fromAttunementDataString(String attunementModificationDataString) {
        return null;
    }

    @Override
    public void applyModification(ItemStack stack) {
        Artifactory.LOGGER.info("Applying attribute modifier " + attribute + " " + operation.toString() + " " + value + " to " + stack.getDisplayName());
    }
}
