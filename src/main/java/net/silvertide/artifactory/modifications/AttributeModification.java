package net.silvertide.artifactory.modifications;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.GUIUtil;
import net.silvertide.artifactory.util.StackNBTUtil;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class AttributeModification implements AttunementModification {
    public static final String ATTRIBUTE_MODIFICATION_TYPE = "attribute";
    public static final String ATTRIBUTE_UUID_KEY = "attribute_uuid";
    public static final String ATTIBUTE_KEY = "attribute";
    public static final String OPERATION_KEY = "operation";
    public static final String VALUE_KEY = "value";
    public static final String EQUIPMENT_SLOT_KEY = "equipment_slot";

    private final String attribute;
    private final int operation;
    private final double value;
    private final UUID attributeUUID;
    private final String equipmentSlotName;

    private AttributeModification(String attribute, int operation, double value, String equipmentSlotName, UUID attributeUUID) {
        this.attribute = attribute;
        this.operation = operation;
        this.value = value;
        this.equipmentSlotName = equipmentSlotName;
        this.attributeUUID = attributeUUID;
    }

    public String getName() {
        return "Artifactory " + attribute;
    }
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.byName(equipmentSlotName);
    }

    public String getAttribute() {
        return this.attribute;
    }
    public ResourceLocation getAttributeResourceLocation() {
        return ResourceLocation.parse(this.attribute);
    }

    public int getOperation() {
        return this.operation;
    }

    public String getEquipmentSlotName() {
        return this.equipmentSlotName;
    }

    public double getValue() {
        return this.value;
    }

    @Nullable
    public static AttributeModification fromAttunementDataString(String attributeModificationDataString) {
        String[] modification = attributeModificationDataString.split("/");
        if(modification.length == 5) {
            int operation = getOperationInteger(modification[2]);
            if(operation != -1) {
                String attribute = modification[1];
                double value = Double.parseDouble(modification[3]);
                String equipmentSlot = modification[4];

                if(isValidEquipmentSlot(equipmentSlot)){
                    return new AttributeModification(attribute, operation, value, equipmentSlot, UUID.randomUUID());
                }
            }
        }
        return null;
    }

    private static boolean isValidEquipmentSlot(String equipmentSlot) {
        try {
            EquipmentSlot.byName(equipmentSlot);
        } catch(IllegalArgumentException e) {
            Artifactory.LOGGER.warn(equipmentSlot + " is not a valid equipment slot. Use mainhand, offhand, head, chest, legs, or feet.");
            return false;
        }
        return true;
    }

    public static Optional<AttributeModification> fromCompoundTag(CompoundTag attributeModificationCompoundTag) {
        String attribute = attributeModificationCompoundTag.getString(ATTIBUTE_KEY);
        if (!attribute.equals("")) {
            int operation = attributeModificationCompoundTag.getInt(OPERATION_KEY);
            double value = attributeModificationCompoundTag.getDouble(VALUE_KEY);
            UUID attributeUUID = attributeModificationCompoundTag.getUUID(ATTRIBUTE_UUID_KEY);
            String equipmentSlotName = attributeModificationCompoundTag.getString(EQUIPMENT_SLOT_KEY);
            return Optional.of(new AttributeModification(attribute, operation, value, equipmentSlotName, attributeUUID));
        }
        return Optional.empty();
    }

    public void addAttributeModifier(ItemAttributeModifierEvent itemAttributeModifierEvent, EquipmentSlotGroup slotGroup) {
        Optional<Holder.Reference<Attribute>> attributeToModify = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(attribute));
        attributeToModify.ifPresent(attributeReference -> {
            itemAttributeModifierEvent.addModifier(attributeReference, this.buildAttributeModifier(), slotGroup);
        });
    }



    private AttributeModifier buildAttributeModifier() {
        return new AttributeModifier(this.getAttributeResourceLocation(), value, AttributeModifier.Operation.BY_ID.apply(operation));
    }

    private static int getOperationInteger(String operation) {
        switch(operation) {
            case "add_value": return 0;
            case "add_multiplied_base": return 1;
            case "add_multiplied_total": return 2;
            default: return -1;
        }
    }

    @Override
    public void applyModification(ItemStack stack) {
        Attribute attributeToModify = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(attribute));
        if(attributeToModify != null) {
            boolean successfullyAddedToExistingAttr = StackNBTUtil.attemptToAddToExistingAttributeUUID(stack, this);
            if(!successfullyAddedToExistingAttr) {
                StackNBTUtil.addAttributeModificaftionTag(stack, attributeUUID, createAttributeModificationTag());
            }
        }
    }

    private CompoundTag createAttributeModificationTag() {
        CompoundTag modificationTag = new CompoundTag();
        modificationTag.putUUID(ATTRIBUTE_UUID_KEY, attributeUUID);
        modificationTag.putString(ATTIBUTE_KEY, attribute);
        modificationTag.putInt(OPERATION_KEY, operation);
        modificationTag.putDouble(VALUE_KEY, value);
        modificationTag.putString(EQUIPMENT_SLOT_KEY, equipmentSlotName);
        return modificationTag;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("+");
        if(operation == 0) {
            result.append(this.value);
        } else {
            result.append(this.value * 100).append("%");
        }

        if(operation == 1) {
            result.append(" Base");
        }

        result.append(" ").append(GUIUtil.prettifyName(attribute));

        return result.toString();
    }
}
