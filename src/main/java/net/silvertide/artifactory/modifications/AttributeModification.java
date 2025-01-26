package net.silvertide.artifactory.modifications;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.DataComponentUtil;
import net.silvertide.artifactory.util.GUIUtil;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record AttributeModification(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) implements AttunementModification {
    public static final Codec<AttributeModification> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeModification> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Attribute.CODEC.fieldOf("attribute").forGetter(AttributeModification::attribute),
                        AttributeModifier.CODEC.fieldOf("modifier").forGetter(AttributeModification::modifier),
                        EquipmentSlotGroup.CODEC.fieldOf("slot").forGetter(AttributeModification::slot))
                .apply(instance, AttributeModification::new));

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttributeModification decode(@NotNull RegistryFriendlyByteBuf buf) {
                return new AttributeModification(Attribute.STREAM_CODEC.decode(buf),
                        AttributeModifier.STREAM_CODEC.decode(buf),
                        EquipmentSlotGroup.STREAM_CODEC.decode(buf));
            }
            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull AttributeModification attunementData) {
                Attribute.STREAM_CODEC.encode(buf, attunementData.attribute());
                AttributeModifier.STREAM_CODEC.encode(buf, attunementData.modifier());
                EquipmentSlotGroup.STREAM_CODEC.encode(buf, attunementData.slot());
            }
        };
    }

    @Nullable
    public static AttributeModification fromAttunementDataString(String attributeModificationDataString) {
        //"attribute/minecraft:generic.attack_damage/add_value/5/mainhand"
        String[] modification = attributeModificationDataString.split("/");
        if(modification.length == 5) {
            int operation = getOperationInteger(modification[2]);
            if(operation != -1) {
                String attribute = modification[1];
                double value;
                try {
                    value = Double.parseDouble(modification[3]);
                }  catch (NumberFormatException exception) {
                    Artifactory.LOGGER.warn("Attribute value could not be parsed into a number (" + modification[3] + ")");
                    return null;
                }

                EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(EquipmentSlot.byName(modification[4]));
                Optional<Holder.Reference<Attribute>> attributeToModify = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(attribute));

                return attributeToModify.map(attributeReference -> {
                    AttributeModifier attributeModifier = buildAttributeModifier(attribute, value, operation);
                    return new AttributeModification(attributeReference, attributeModifier, equipmentSlotGroup);
                }).orElse(null);
            } else {
                Artifactory.LOGGER.warn("Attribute operation formatted incorrectly. Operation must be \"add_value\", \"add_multiplied_base\", or \"add_multiplied_total\"");
            }
        }
        return null;
    }

    public void addAttributeModifier(ItemAttributeModifierEvent itemAttributeModifierEvent) {
        itemAttributeModifierEvent.addModifier(this.attribute, this.modifier, this.slot());
    }

    public void addAttributeModifier(CurioAttributeModifierEvent curioAttributeModifierEvent) {
        curioAttributeModifierEvent.addModifier(this.attribute, this.modifier);
    }

    private static AttributeModifier buildAttributeModifier(String resourceLocation, double value, int operation) {
        ResourceLocation location = ResourceLocation.parse(resourceLocation);
        AttributeModifier.Operation modifierOperation = AttributeModifier.Operation.BY_ID.apply(operation);
        return new AttributeModifier(location, value, modifierOperation);
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
        DataComponentUtil.getAttunementData(stack).ifPresent(attunementData -> {
            List<AttributeModification> newModifications = new ArrayList<>(attunementData.attributeModifications());

            boolean wasCombined = false;
            for(int i = 0; i < newModifications.size(); i++) {
                AttributeModification currModification = newModifications.get(i);
                if(this.attribute().value().getDescriptionId().equals(currModification.attribute().value().getDescriptionId()) && this.slot() == currModification.slot()) {
                    newModifications.set(i, combineWith(currModification));
                    wasCombined = true;
                    break;
                }
            }

            if(!wasCombined) {
                newModifications.add(this);
            }

            DataComponentUtil.setAttunementData(stack, attunementData.withAttributeModifications(newModifications));
        });
    }

    public AttributeModification combineWith(AttributeModification existingAttributeModification) {
        AttributeModifier existingModifier = existingAttributeModification.modifier();
        AttributeModifier newModifier = new AttributeModifier(existingModifier.id(), this.modifier().amount() + existingModifier.amount(), existingModifier.operation());
        return new AttributeModification(existingAttributeModification.attribute(), newModifier, existingAttributeModification.slot());
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("+");
        if(modifier().operation().id() == 0) {
            result.append(modifier().amount());
        } else {
            result.append(modifier().amount() * 100).append("%");
        }

        if(modifier().operation().id() == 1) {
            result.append(" Base");
        }

        if(attribute().isBound()) {
            result.append(" ").append(GUIUtil.prettifyName(attribute().value().getDescriptionId()));
        } else {
            result.append(" unknown");
        }
        return result.toString();
    }
}
