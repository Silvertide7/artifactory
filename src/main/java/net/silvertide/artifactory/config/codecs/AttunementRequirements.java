package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record AttunementRequirements(int xpLevelsConsumed, int xpLevelThreshold, int kills, List<String> items, List<String> feats) {
    public static final Codec<AttunementRequirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("xpLevelsConsumed", -1).forGetter(AttunementRequirements::xpLevelsConsumed),
                    Codec.INT.optionalFieldOf("xpLevelThreshold", -1).forGetter(AttunementRequirements::xpLevelThreshold),
                    Codec.INT.optionalFieldOf("kills", 0).forGetter(AttunementRequirements::kills),
                    Codec.list(Codec.STRING).optionalFieldOf("items", new ArrayList<>()).forGetter(AttunementRequirements::items),
                    Codec.list(Codec.STRING).optionalFieldOf("feats", new ArrayList<>()).forGetter(AttunementRequirements::feats))
            .apply(instance, AttunementRequirements::new)
    );

    public String toString() {
        StringBuilder result = new StringBuilder("AttunementRequirements: \n" +
                "\txpLevelsThreshold : " + xpLevelThreshold() + "\n" +
                "\txpLevelsConsumed : " + xpLevelsConsumed() + "\n" +
                "\tkills: " + kills() + "\n" +
                "\titems: {\n");

        for(String item : items) {
            result.append("\t").append(item).append(" \n");
        }

        result.append("\tfeats: {");
        for(String feat : feats) {
            result.append("\t").append(feat).append(" \n");
        }

        return result.append("}").toString();
    }

    public static AttunementRequirements getDefault() {
        return new AttunementRequirements(-1, -1, 0, new ArrayList<>(), new ArrayList<>());
    }
}
