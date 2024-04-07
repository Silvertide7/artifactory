package net.silvertide.artifactory.modifications;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.silvertide.artifactory.Artifactory;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public class AttributeModification implements AttunementModification {
    public static final String ATTRIBUTE_MODIFICATION_TYPE = "ATTRIBUTE";
    private String attribute;
    private int operation;
    private double value;
    private UUID attributeUUID;

    private AttributeModification(String attribute, int operation, double value) {
        this.attribute = attribute;
        this.operation = operation;
        this.value = value;
        this.attributeUUID = UUID.randomUUID();
    }
    public static Optional<AttunementModification> fromAttunementDataString(String attunementModificationDataString) {
//        Item NBT: {AttributeModifiers:[{Amount:20.0d,AttributeName:"minecraft:generic.attack_damage",Name:"generic.attack_damage",Operation:0,Slot:"mainhand",UUID:[I;-192596,-61963,-161679,-10288428]}],Damage:0}
//        "attribute/minecraft:generic.attack_damage/20.0/addition;"
        String[] modification = attunementModificationDataString.split("/");
        if(modification.length == 4) {
            int operation = getOperationInteger(modification[2]);
            if(operation != -1) {
                String attribute = modification[1];
                double value = Double.parseDouble(modification[3]);
                return Optional.of(new AttributeModification(attribute, operation, value));
            }
        }
        return Optional.empty();
    }

    private static int getOperationInteger(String operation) {
        switch(operation) {
            case "addition": return 0;
            case "multiply_base": return 1;
            case "multiply_total": return 2;
            default: return -1;
        }
    }

    @Override
    public void applyModification(ItemStack stack) {
        Attribute attributeToModify = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attribute));
        if(attributeToModify != null) {
            // Store the information in the artifactory:modifications tag. attribute: {}
            // generate a new UUID for the modifier to store with it
        }
    }

    @Override
    public String toString() {
        return "AttributeModification/" + attribute + "/" + operation + "/" + value + "/" + attributeUUID;
    }

    //    private CompoundTag getOrCreateAttributeModifiersTag(ItemStack stack) {
//
//
//    }
}
