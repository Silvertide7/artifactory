package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;

public record AttunementLevel(List<String> modifications, AttunementRequirements requirements) {
    public static final Codec<AttunementLevel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.list(Codec.STRING).fieldOf("modifications").forGetter(AttunementLevel::modifications),
                    AttunementRequirements.CODEC.optionalFieldOf("requirements", AttunementRequirements.getDefault()).forGetter(AttunementLevel::requirements))
            .apply(instance, AttunementLevel::new)
    );

    public String toString() {
        StringBuilder result = new StringBuilder("AttunementLevel: \n" +
                " modifications: [");

        for (String modification : modifications) {
            result.append("\t").append(modification).append("\n");
        }

        result.append("requirements:").append(requirements);

        return result.toString();
    }

    public static AttunementLevel getDefault() {
        List<String> defaultModifications = new ArrayList<>(Arrays.asList("unbreakable", "invulnerable", "soulbound"));
        return new AttunementLevel(defaultModifications, AttunementRequirements.getDefault());
    }
}
