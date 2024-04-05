package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record AttunementRequirements(int xpLevels, int kills, List<String> feats) {
    public static final Codec<AttunementRequirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("xpLevels", 0).forGetter(AttunementRequirements::xpLevels),
                    Codec.INT.optionalFieldOf("kills", 0).forGetter(AttunementRequirements::kills),
                    Codec.list(Codec.STRING).optionalFieldOf("feats", new ArrayList<>()).forGetter(AttunementRequirements::feats))
            .apply(instance, AttunementRequirements::new)
    );

    public String toString() {
        StringBuilder result = new StringBuilder("AttunementRequirements: \n" +
                "\txpLevels : " + xpLevels() + "\n" +
                "\tkills: " + kills() + "\n" +
                "\tfeats: {\n");

        for(String feat : feats){
            result.append("\t").append(feat).append(" \n");
        }
        return result.append("}").toString();
    }

    public static AttunementRequirements getDefault() {
        return new AttunementRequirements(0, 0, new ArrayList<>());
    }
}
