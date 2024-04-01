package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record ItemAttunementData(int attunementSlotsUsed, Map<String, List<String>> modifications, boolean useWithoutAttunement, boolean replace) {
    public static final Codec<ItemAttunementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("slots_used").forGetter(ItemAttunementData::attunementSlotsUsed),
            Codec.unboundedMap(Codec.STRING, Codec.list(Codec.STRING)).fieldOf("modifications").forGetter(ItemAttunementData::modifications),
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

        for(Map.Entry<String, List<String>> attunementLevel :  modifications.entrySet()){
            String levelOfAttunement = attunementLevel.getKey();
            List<String> effects = attunementLevel.getValue();

            attunementString.append(levelOfAttunement).append(": \n");
            for (String effect : effects) {
                attunementString.append("  - ").append(effect).append("\n");
            }
        }


        return "replace: " + replace + "\n" +
                "attunement_slots_used: " + getAttunementSlotsUsed() + "\n" +
                "use_without_attunement: " + useWithoutAttunement + "\n" +
                attunementString;
    }
}
