package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record AttunementDataSource(int attunementSlotsUsed, List<AttunementLevel> attunementLevels, boolean useWithoutAttunement, boolean unique, boolean replace) {
    public static final Codec<AttunementDataSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("slots_used", -1).forGetter(AttunementDataSource::attunementSlotsUsed),
            Codec.list(AttunementLevel.CODEC).optionalFieldOf("attunement_levels", getDefaultAttunementLevels()).forGetter(AttunementDataSource::attunementLevels),
            Codec.BOOL.optionalFieldOf("use_without_attunement", true).forGetter(AttunementDataSource::useWithoutAttunement),
            Codec.BOOL.optionalFieldOf("unique", false).forGetter(AttunementDataSource::unique),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(AttunementDataSource::replace))
            .apply(instance, AttunementDataSource::new)
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
