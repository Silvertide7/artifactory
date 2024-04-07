package net.silvertide.artifactory.modifications;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.silvertide.artifactory.Artifactory;

import java.util.Optional;

public class AttributeModification implements AttunementModification {
    public static final String ATTRIBUTE_MODIFICATION_TYPE = "ATTRIBUTE";
    private String attribute;
    private int operation;
    private double value;

    private AttributeModification(String attribute, int operation, double value) {
        this.attribute = attribute;
        this.operation = operation;
        this.value = value;
    }
    public static Optional<AttunementModification> fromAttunementDataString(String attunementModificationDataString) {
//        Item NBT: {AttributeModifiers:[{Amount:20.0d,AttributeName:"minecraft:generic.attack_damage",Name:"generic.attack_damage",Operation:0,Slot:"mainhand",UUID:[I;-192596,-61963,-161679,-10288428]}],Damage:0}
//        "attribute/minecraft:generic.attack_damage/20.0/addition;"
//        ADDITION(0),
//        MULTIPLY_BASE(1),
//        MULTIPLY_TOTAL(2);

        return Optional.of(new AttributeModification("minecraft:generic.attack_damage", 0, 10));
    }

    @Override
    public void applyModification(ItemStack stack) {
        Attribute attributeToModify = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attribute));
        if(attributeToModify != null) {
            // Store the information in the artifactory:modifications tag. attribute: {}
            // generate a new UUID for the modifier to store with it
        }
    }

//    private CompoundTag getOrCreateAttributeModifiersTag(ItemStack stack) {
//
//
//    }
}
