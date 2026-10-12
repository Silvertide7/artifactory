package net.silvertide.artifactory.component.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record ComponentPresentCondition(ResourceLocation component, boolean present, Optional<Component> description) implements AttunementCondition {
    public static final String TYPE = "artifactory:component_present";

    public static final MapCodec<ComponentPresentCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("component").forGetter(ComponentPresentCondition::component),
                    Codec.BOOL.optionalFieldOf("present", true).forGetter(ComponentPresentCondition::present),
                    ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(ComponentPresentCondition::description))
            .apply(instance, ComponentPresentCondition::new));

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public boolean isMetBy(ItemStack stack) {
        return switch (ConditionComponentTypes.lookUp(component)) {
            case ConditionComponentTypes.OwningModAbsent ignored -> true;
            case ConditionComponentTypes.MissingFromLoadedMod ignored -> false;
            case ConditionComponentTypes.Found found -> stack.has(found.componentType()) == present;
        };
    }

    @Override
    public Component failureDescription() {
        return description.orElseGet(() -> Component.translatable(present
                ? "artifactory.condition.component_present.required"
                : "artifactory.condition.component_present.forbidden", component.toString()));
    }
}
