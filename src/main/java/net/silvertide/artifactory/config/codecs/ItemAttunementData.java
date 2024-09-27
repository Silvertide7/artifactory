package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.silvertide.artifactory.Artifactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ItemAttunementData(int attunementSlotsUsed, List<AttunementLevel> attunementLevels, boolean useWithoutAttunement, boolean replace) {

    // Validation method for ensuring the string is a number in the range 1-10000
    private static DataResult<String> validateStringNumber(String input) {
        try {
            int value = Integer.parseInt(input);
            if (value >= 1 && value <= 10000) {
                return DataResult.success(input);
            } else {
                Artifactory.LOGGER.error("Attunement Level value must be between 1 and 10000");
                return DataResult.error(() -> "Key must be a number between 1 and 10000, found: " + value);
            }
        } catch (NumberFormatException e) {
            Artifactory.LOGGER.error("Attunement Level error reading in key from datapack. " + input + " is not an allowed value. Using defaults Attunement Level.");
            return DataResult.error(() -> "Invalid number format for key: " + input);
        }
    }

    public static final Codec<ItemAttunementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("slots_used", -1).forGetter(ItemAttunementData::attunementSlotsUsed),
            Codec.list(AttunementLevel.CODEC).optionalFieldOf("attunement_levels", new ArrayList<>()).forGetter(ItemAttunementData::attunementLevels),
            Codec.BOOL.optionalFieldOf("use_without_attunement", true).forGetter(ItemAttunementData::useWithoutAttunement),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(ItemAttunementData::replace))
            .apply(instance, ItemAttunementData::new)
    );

    public int getAttunementSlotsUsed() {
        if(attunementSlotsUsed < 0) return 0;
        return attunementSlotsUsed;
    }

    public String toString() {
        StringBuilder attunementString = new StringBuilder();

        for(int i = 0; i < attunementLevels().size(); i++) {
            attunementString.append("Level ").append(i + 1).append(": ").append(attunementLevels().get(i)).append(": \n");

        }

        return "replace: " + replace + "\n" +
                "attunement_slots_used: " + getAttunementSlotsUsed() + "\n" +
                "use_without_attunement: " + useWithoutAttunement + "\n" +
                attunementString;
    }

    private static Map<String, AttunementLevel> createDefaultAttunementsMap() {
        Map<String, AttunementLevel> defaultAttunementsMap = new HashMap<>();
        defaultAttunementsMap.put("1", AttunementLevel.getDefault());
        return defaultAttunementsMap;
    }
}
