package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public class AttunementRequirements {
    int xpLevelsConsumed, xpLevelThreshold, kills;
    List<String> items, feats;

    public AttunementRequirements(int xpLevelsConsumed, int xpLevelThreshold, int kills, List<String> items, List<String> feats) {
        this.xpLevelsConsumed = xpLevelsConsumed;
        this.xpLevelThreshold = xpLevelThreshold;
        this.kills = kills;
        this.items = items;
        this.feats = feats;
    }

    public static final Codec<AttunementRequirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("xpLevelsConsumed", -1).forGetter(AttunementRequirements::getXpLevelsConsumed),
                    Codec.INT.optionalFieldOf("xpLevelThreshold", -1).forGetter(AttunementRequirements::getXpLevelThreshold),
                    Codec.INT.optionalFieldOf("kills", 0).forGetter(AttunementRequirements::getKills),
                    Codec.list(Codec.STRING).optionalFieldOf("items", new ArrayList<>()).forGetter(AttunementRequirements::getItems),
                    Codec.list(Codec.STRING).optionalFieldOf("feats", new ArrayList<>()).forGetter(AttunementRequirements::getFeats))
            .apply(instance, AttunementRequirements::new)
    );

    public int getXpLevelsConsumed() {
        return xpLevelsConsumed;
    }

    public void setXpLevelsConsumed(int xpLevelsConsumed) {
        this.xpLevelsConsumed = xpLevelsConsumed;
    }

    public int getXpLevelThreshold() {
        return xpLevelThreshold;
    }

    public void setXpLevelThreshold(int xpLevelThreshold) {
        this.xpLevelThreshold = xpLevelThreshold;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<String> getFeats() {
        return feats;
    }

    public void setFeats(List<String> feats) {
        this.feats = feats;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("AttunementRequirements: \n" +
                "\txpLevelsThreshold : " + getXpLevelThreshold() + "\n" +
                "\txpLevelsConsumed : " + getXpLevelsConsumed() + "\n" +
                "\tkills: " + getKills() + "\n" +
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
