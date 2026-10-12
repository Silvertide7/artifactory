package net.silvertide.artifactory.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.compat.CompatFlags;
import net.silvertide.artifactory.component.AttunementLevel;
import net.silvertide.artifactory.component.AttunementOverride;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.component.condition.AttunementCondition;
import net.silvertide.artifactory.component.condition.AttunementConditions;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.storage.AttunedItem;
import net.silvertide.silverlib.feature.breakage.BrokenItems;

import java.util.List;
import java.util.Optional;

public final class AttunementSchemaUtil {
    private AttunementSchemaUtil() {}

    public static Optional<AttunementSchema> getAttunementSchema(ItemStack stack) {
        AttunementOverride override = DataComponentUtil.getAttunementOverride(stack);
        if(override.isValidSchema()) return Optional.of(override);

        Optional<AttunementDataSource> source = AttunementDataSourceUtil.getAttunementDataSource(stack);
        if(source.isPresent() && source.get().isValidSchema() && isAttunedOrApplies(stack, source.get())) return Optional.of(source.get());

        return Optional.empty();
    }

    private static boolean isAttunedOrApplies(ItemStack stack, AttunementDataSource source) {
        if(DataComponentUtil.isActivelyAttuned(stack)) return true;
        return meetsMinQuality(stack, source) && source.appliesWhen().stream().allMatch(condition -> condition.isMetBy(stack));
    }

    private static boolean meetsMinQuality(ItemStack stack, AttunementDataSource source) {
        return source.minQuality()
                .map(minQuality -> CompatFlags.QUALITY_LOADED && net.silvertide.artifactory.compat.QualityCompat.hasQualityAtLeast(stack, minQuality))
                .orElse(true);
    }

    public static List<Component> describeUnmetAppliesWhenConditions(ItemStack stack) {
        if(DataComponentUtil.isActivelyAttuned(stack) || hasRolledNonAttunable(stack)) return List.of();
        return AttunementDataSourceUtil.getAttunementDataSource(stack)
                .filter(AttunementDataSource::isValidSchema)
                .filter(source -> meetsMinQuality(stack, source))
                .filter(source -> !AttunementConditions.isUnreadable(source.appliesWhen()))
                .map(source -> source.appliesWhen().stream()
                        .filter(condition -> !condition.isMetBy(stack))
                        .map(AttunementCondition::failureDescription)
                        .toList())
                .orElse(List.of());
    }

    public static boolean meetsAttunementConditions(AttunementSchema attunementSchema, ItemStack stack) {
        return attunementSchema.attunementConditions().stream().allMatch(condition -> condition.isMetBy(stack));
    }

    public static List<Component> describeUnmetAttunementConditions(AttunementSchema attunementSchema, ItemStack stack) {
        return attunementSchema.attunementConditions().stream()
                .filter(condition -> !condition.isMetBy(stack))
                .map(AttunementCondition::failureDescription)
                .toList();
    }

    private static boolean hasRolledNonAttunable(ItemStack stack) {
        return DataComponentUtil.getAttunementFlag(stack).map(flag -> flag.discovered() && !flag.isAttunable()).orElse(false);
    }

    public static boolean hasValidSchema(ItemStack stack) {
        return getAttunementSchema(stack).isPresent();
    }

    public static boolean shouldApplyAttunementModifications(ItemStack stack) {
        return DataComponentUtil.isActivelyAttuned(stack) && hasValidSchema(stack) && !BrokenItems.isBroken(stack);
    }

    public static Optional<AttunementSchema> getAttunementSchema(AttunedItem attunedItem) {
        if(attunedItem.getAttunementOverrideOpt().isPresent()) return Optional.of(attunedItem.getAttunementOverrideOpt().get());

        Optional<AttunementDataSource> source = AttunementDataSourceUtil.getAttunementDataSource(attunedItem.getResourceLocation());
        if(source.isPresent() && source.get().isValidSchema()) return Optional.of(source.get());

        return Optional.empty();
    }

    public static AttunementLevel getAttunementLevel(ItemStack stack, int level) {
        return getAttunementSchema(stack).map(attunementData -> {
            if(attunementData.attunementLevels().isEmpty()) return null;
            return attunementData.attunementLevels().get(level - 1);
        }).orElse(null);
    }

    public static int getNumAttunementLevels(ItemStack stack) {
        return getAttunementSchema(stack).map(attunementData -> attunementData.attunementLevels().size()).orElse(0);
    }

    public static String getAttunementLevelDescriptions(AttunementSchema attunementSchema) {
        // "minecraft:diamond_sword;1#soulbound,invulnerable,attack~2#unbreakable"
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < attunementSchema.attunementLevels().size(); i++) {
            AttunementLevel attunementLevel = attunementSchema.attunementLevels().get(i);
            stringBuilder.append(i+1).append("#").append(attunementLevel.getModificationsStringList());
            if (i != attunementSchema.attunementLevels().size() - 1) stringBuilder.append("~");
        }
        return stringBuilder.toString();
    }

    public static boolean canUseWithoutAttunement(ItemStack stack) {
        return getAttunementSchema(stack).map(AttunementSchema::useWithoutAttunement).orElse(false);
    }
}
