package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;

public record AttunementLevel(AttunementRequirements requirements, List<String> modifications) {
    public static final Codec<AttunementLevel> CODEC;

    private AttunementRequirements requirements;
    private List<String> modifications;


    public AttunementLevel(List<String> modifications, AttunementRequirements requirements) {
        this.requirements = requirements;
        this.modifications = modifications;
    }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(Codec.STRING).optionalFieldOf("modifications", new ArrayList<>()).forGetter(AttunementLevel::getModifications),
                AttunementRequirements.CODEC.optionalFieldOf("requirements", AttunementRequirements.getDefault()).forGetter(AttunementLevel::getRequirements))
            .apply(instance, AttunementLevel::new)
        );
    }



    public List<String> getModifications() {
        return this.modifications;
    }

    public void setRequirements(AttunementRequirements requirements) {
        this.requirements = requirements;
    }

    public AttunementRequirements getRequirements() {
        return this.requirements;
    }

    public void setModifications(List<String> modifications) {
        this.modifications = modifications;
    }

    public String getModificationsStringList() {
        StringBuilder result = new StringBuilder();

        if(modifications.isEmpty()) return "NONE";

        for(int i = 0; i < modifications.size(); i++){
            result.append(modifications.get(i));
            if(i != modifications.size() - 1 ){
                result.append(',');
            }
        }

        return result.toString();
    }

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
