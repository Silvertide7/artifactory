package net.silvertide.artifactory.util;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.AttunementLevel;
import net.silvertide.artifactory.component.AttunementOverride;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;

import java.util.Optional;

public final class AttunementSchemaUtil {
    private AttunementSchemaUtil() {}

    public static Optional<AttunementSchema> getAttunementSchema(ItemStack stack) {
        Optional<AttunementOverride> override = DataComponentUtil.getAttunementOverride(stack);
        if(override.isPresent()) return Optional.of(override.get());

        Optional<AttunementDataSource> source = AttunementDataSourceUtil.getAttunementDataSource(stack);
        if(source.isPresent()) return Optional.of(source.get());

        return Optional.empty();
    }

    public static AttunementLevel getAttunementLevel(ItemStack stack, int level) {
        return getAttunementSchema(stack).map(attunementData -> attunementData.attunementLevels().get(level - 1)).orElse(null);
    }

    public static int getNumAttunementLevels(ItemStack stack) {
        return getAttunementSchema(stack).map(attunementData -> attunementData.attunementLevels().size()).orElse(0);
    }

    public static String getAttunementLevelDescriptions(AttunementSchema attunementSchema, String resourceLocation, int currentAttunementLevel) {
        // "minecraft:diamond_sword;1#soulbound,invulnerable,attack~2#unbreakable"
        StringBuilder stringBuilder = new StringBuilder(resourceLocation + ";");

        for(int i = 0; i < attunementSchema.attunementLevels().size(); i++) {
            AttunementLevel attunementLevel = attunementSchema.attunementLevels().get(i);
            if(shouldSendAttunementLevelInformation(i, currentAttunementLevel)){
                stringBuilder.append(i+1).append("#").append(attunementLevel.getModificationsStringList());
                if (i != attunementSchema.attunementLevels().size() - 1) stringBuilder.append("~");
            }
        }
        return stringBuilder.toString();
    }

    private static boolean shouldSendAttunementLevelInformation(int level, int currentAttunementLevel) {
        String currentInformationLevel = ServerConfigs.ATTUNEMENT_INFORMATION_EXTENT.get();
        if("all".equals(currentInformationLevel)) return true;
        else {
            if("next".equals(currentInformationLevel)) return level < currentAttunementLevel + 1;
            if("current".equals(currentInformationLevel)) return level <= currentAttunementLevel;
        }
        return false;
    }

    public static boolean canUseWithoutAttunement(ItemStack stack) {
        return getAttunementSchema(stack).map(AttunementSchema::useWithoutAttunement).orElse(false);
    }

    public static boolean isUniqueAttunement(ItemStack stack) {
        return getAttunementSchema(stack).map(AttunementSchema::unique).orElse(false);
    }

}
