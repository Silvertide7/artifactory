package net.silvertide.artifactory.component.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalInt;

public record ComponentIntRangeCondition(ResourceLocation component, Optional<Integer> min, Optional<Integer> max, WhenMissing whenMissing, Optional<Component> description) implements AttunementCondition {
    public static final String TYPE = "artifactory:component_int_range";

    public static final MapCodec<ComponentIntRangeCondition> MAP_CODEC = RecordCodecBuilder.<ComponentIntRangeCondition>mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("component").forGetter(ComponentIntRangeCondition::component),
                    Codec.INT.optionalFieldOf("min").forGetter(ComponentIntRangeCondition::min),
                    Codec.INT.optionalFieldOf("max").forGetter(ComponentIntRangeCondition::max),
                    WhenMissing.CODEC.optionalFieldOf("when_missing", WhenMissing.FAIL).forGetter(ComponentIntRangeCondition::whenMissing),
                    ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(ComponentIntRangeCondition::description))
            .apply(instance, ComponentIntRangeCondition::new))
            .validate(condition -> condition.min().isEmpty() && condition.max().isEmpty()
                    ? DataResult.error(() -> TYPE + " for " + condition.component() + " needs a min, a max, or both")
                    : DataResult.success(condition));

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
        if (!stack.has(componentType)) return whenMissing == WhenMissing.PASS;
        OptionalInt value = readAsInt(componentType, stack);
        if (value.isEmpty()) {
            ConditionComponentTypes.reportUnreadableOnce(TYPE, component, "does not hold a whole number");
            return false;
        }
        int level = value.getAsInt();
        return min.map(minimum -> level >= minimum).orElse(true) && max.map(maximum -> level <= maximum).orElse(true);
    }

    @Override
    public Component failureDescription() {
        return description.orElseGet(this::defaultFailureDescription);
    }

    private Component defaultFailureDescription() {
        String componentName = component.toString();
        if (min.isPresent() && max.isPresent()) {
            return Component.translatable("artifactory.condition.component_int_range.between", componentName, min.get(), max.get());
        }
        return min.<Component>map(minimum -> Component.translatable("artifactory.condition.component_int_range.at_least", componentName, minimum))
                .orElseGet(() -> Component.translatable("artifactory.condition.component_int_range.at_most", componentName, max.orElseThrow()));
    }

    private static <T> OptionalInt readAsInt(DataComponentType<T> componentType, ItemStack stack) {
        T value = stack.get(componentType);
        if (value instanceof Integer integerValue) return OptionalInt.of(integerValue);
        if (componentType.codec() == null) return OptionalInt.empty();
        return componentType.codec().encodeStart(NbtOps.INSTANCE, value).result()
                .map(ComponentIntRangeCondition::wholeNumberIn)
                .orElse(OptionalInt.empty());
    }

    private static OptionalInt wholeNumberIn(Tag tag) {
        if (tag instanceof IntTag || tag instanceof ShortTag || tag instanceof ByteTag) return OptionalInt.of(((NumericTag) tag).getAsInt());
        if (tag instanceof LongTag longTag && longTag.getAsLong() >= Integer.MIN_VALUE && longTag.getAsLong() <= Integer.MAX_VALUE) {
            return OptionalInt.of((int) longTag.getAsLong());
        }
        return OptionalInt.empty();
    }

    public enum WhenMissing implements StringRepresentable {
        PASS("pass"),
        FAIL("fail");

        public static final Codec<WhenMissing> CODEC = StringRepresentable.fromEnum(WhenMissing::values);
        private final String serializedName;

        WhenMissing(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public @NotNull String getSerializedName() {
            return serializedName;
        }
    }
}
