package net.silvertide.artifactory.component.condition;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

public final class ComponentEqualsCondition implements AttunementCondition {
    public static final String TYPE = "artifactory:component_equals";

    public static final MapCodec<ComponentEqualsCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("component").forGetter(ComponentEqualsCondition::component),
                    ExtraCodecs.JSON.fieldOf("value").forGetter(ComponentEqualsCondition::value),
                    ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(ComponentEqualsCondition::description))
            .apply(instance, ComponentEqualsCondition::new));

    private final ResourceLocation component;
    private final JsonElement value;
    private final Optional<Component> description;
    private volatile Optional<Object> decodedExpectedValue;

    public ComponentEqualsCondition(ResourceLocation component, JsonElement value, Optional<Component> description) {
        this.component = component;
        this.value = value;
        this.description = description;
    }

    public ResourceLocation component() {
        return component;
    }

    public JsonElement value() {
        return value;
    }

    public Optional<Component> description() {
        return description;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public boolean isMetBy(ItemStack stack) {
        return switch (ConditionComponentTypes.lookUp(component)) {
            case ConditionComponentTypes.OwningModAbsent ignored -> true;
            case ConditionComponentTypes.MissingFromLoadedMod ignored -> false;
            case ConditionComponentTypes.Found found -> isMetByComponentValue(found.componentType(), stack);
        };
    }

    private boolean isMetByComponentValue(DataComponentType<?> componentType, ItemStack stack) {
        Optional<Object> expectedValue = decodedExpectedValue;
        if (expectedValue == null) {
            expectedValue = decodeExpectedValue(componentType);
            decodedExpectedValue = expectedValue;
        }
        if (expectedValue.isEmpty()) {
            ConditionComponentTypes.reportUnreadableOnce(TYPE, component, "cannot read the expected value " + value);
            return false;
        }
        return Objects.equals(stack.get(componentType), expectedValue.get());
    }

    @Override
    public Component failureDescription() {
        return description.orElseGet(() -> Component.translatable("artifactory.condition.component_equals", component.toString(), value.toString()));
    }

    private Optional<Object> decodeExpectedValue(DataComponentType<?> componentType) {
        return parseWithComponentCodec(componentType, value);
    }

    private static <T> Optional<Object> parseWithComponentCodec(DataComponentType<T> componentType, JsonElement json) {
        if (componentType.codec() == null) return Optional.empty();
        return componentType.codec().parse(JsonOps.INSTANCE, json).result().map(Object.class::cast);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ComponentEqualsCondition that
                && component.equals(that.component)
                && value.equals(that.value)
                && description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, value, description);
    }
}
