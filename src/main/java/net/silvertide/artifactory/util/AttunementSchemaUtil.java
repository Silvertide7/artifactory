package net.silvertide.artifactory.util;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.AttunementLevel;
import net.silvertide.artifactory.component.AttunementOverride;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.storage.AttunedItem;

import java.util.Optional;

public final class AttunementSchemaUtil {
    private AttunementSchemaUtil() {}

    public static Optional<AttunementSchema> getAttunementSchema(ItemStack stack) {
        AttunementOverride override = DataComponentUtil.getAttunementOverride(stack);
        if(override.isValidSchema()) return Optional.of(override);

        Optional<AttunementDataSource> source = AttunementDataSourceUtil.getAttunementDataSource(stack);
        if(source.isPresent() && source.get().isValidSchema()) return Optional.of(source.get());

        return Optional.empty();
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
