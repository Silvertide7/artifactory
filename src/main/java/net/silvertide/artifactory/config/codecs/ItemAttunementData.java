package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record ItemAttunementData(int attunementSlotsUsed, List<AttunementLevel> attunementLevels, boolean useWithoutAttunement, boolean unique, List<String> applyToItems, boolean replace) {
    public static final Codec<ItemAttunementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("slots_used", -1).forGetter(ItemAttunementData::attunementSlotsUsed),
            Codec.list(AttunementLevel.CODEC).optionalFieldOf("attunement_levels", getDefaultAttunementLevels()).forGetter(ItemAttunementData::attunementLevels),
            Codec.BOOL.optionalFieldOf("use_without_attunement", true).forGetter(ItemAttunementData::useWithoutAttunement),
            Codec.BOOL.optionalFieldOf("unique", false).forGetter(ItemAttunementData::unique),
            Codec.list(Codec.STRING).optionalFieldOf("apply_to_items", List.of()).forGetter(ItemAttunementData::applyToItems),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(ItemAttunementData::replace))
            .apply(instance, ItemAttunementData::new)
    );

    public int getAttunementSlotsUsed() {
        if(attunementSlotsUsed < 0) return 0;
        return attunementSlotsUsed;
    }

    private static List<AttunementLevel> getDefaultAttunementLevels() {
        return List.of(AttunementLevel.getDefault());
    }

    public String toString() {
        StringBuilder attunementString = new StringBuilder();

        for(int i = 0; i < attunementLevels().size(); i++) {
            attunementString.append("Level ").append(i + 1).append(": ").append(attunementLevels().get(i)).append(": \n");

        }

        return "replace: " + replace() + "\n" +
                "attunement_slots_used: " + getAttunementSlotsUsed() + "\n" +
                "use_without_attunement: " + useWithoutAttunement() + "\n" +
                "unique: " + unique() + "\n" +
                attunementString;
    }
}
