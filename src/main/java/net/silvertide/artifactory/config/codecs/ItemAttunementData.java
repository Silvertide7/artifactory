package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

public record ItemAttunementData(int attunementSlotsUsed, Map<String, AttunementLevel> attunements, boolean useWithoutAttunement, boolean replace) {
    public static final Codec<ItemAttunementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("slots_used").forGetter(ItemAttunementData::attunementSlotsUsed),
            Codec.unboundedMap(Codec.STRING, AttunementLevel.CODEC).optionalFieldOf("attunements", createDefaultAttunementsMap()).forGetter(ItemAttunementData::attunements),
            Codec.BOOL.optionalFieldOf("use_without_attunement", false).forGetter(ItemAttunementData::useWithoutAttunement),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(ItemAttunementData::replace))
            .apply(instance, ItemAttunementData::new)
    );

    public int getAttunementSlotsUsed() {
        if(attunementSlotsUsed < 0) return 0;
        return attunementSlotsUsed;
    }

    public String toString() {
        StringBuilder attunementString = new StringBuilder();

        for(Map.Entry<String, AttunementLevel> attunementLevels :  attunements.entrySet()){
            attunementString.append("Level " + attunementLevels.getKey() + ": " + attunementLevels.getValue()).append(": \n");
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
