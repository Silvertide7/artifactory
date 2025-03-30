package net.silvertide.artifactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AttunementRequirements(int xpLevelsConsumed, int xpLevelThreshold, int kills, List<String> items, List<String> feats) {
    public static final Codec<AttunementRequirements> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementRequirements> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.optionalFieldOf("xp_levels_consumed", -1).forGetter(AttunementRequirements::xpLevelsConsumed),
                        Codec.INT.optionalFieldOf("xp_level_threshold", -1).forGetter(AttunementRequirements::xpLevelThreshold),
                        Codec.INT.optionalFieldOf("kills", 0).forGetter(AttunementRequirements::kills),
                        Codec.list(Codec.STRING).optionalFieldOf("items", new ArrayList<>()).forGetter(AttunementRequirements::items),
                        Codec.list(Codec.STRING).optionalFieldOf("feats", new ArrayList<>()).forGetter(AttunementRequirements::feats))
                .apply(instance, AttunementRequirements::new)
        );

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttunementRequirements decode(@NotNull RegistryFriendlyByteBuf buf) {
                int xpLevelsConsumed = buf.readInt();
                int xpLevelThreshold = buf.readInt();
                int kills = buf.readInt();

                List<String> items = new ArrayList<>();
                int numItems = buf.readVarInt();
                for(int i = 0; i < numItems; i++) {
                    items.add(buf.readUtf());
                }

                List<String> feats = new ArrayList<>();
                int numFeats = buf.readVarInt();
                for(int i = 0; i < numFeats; i++) {
                    feats.add(buf.readUtf());
                }

                return new AttunementRequirements(xpLevelsConsumed, xpLevelThreshold, kills, items, feats);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, AttunementRequirements attunementRequirements) {
                buf.writeInt(attunementRequirements.xpLevelsConsumed());
                buf.writeInt(attunementRequirements.xpLevelThreshold());
                buf.writeInt(attunementRequirements.kills());

                buf.writeVarInt(attunementRequirements.items().size());
                for(int i = 0; i < attunementRequirements.items().size(); i++) {
                    buf.writeUtf(attunementRequirements.items().get(i));
                }

                buf.writeVarInt(attunementRequirements.feats().size());
                for(int i = 0; i < attunementRequirements.feats().size(); i++) {
                    buf.writeUtf(attunementRequirements.feats().get(i));
                }
            }
        };
    }

    public AttunementRequirements withItems(List<String> items) {
        return new AttunementRequirements(this.xpLevelsConsumed(), this.xpLevelThreshold(), kills(), items, this.feats());
    }

    public static AttunementRequirements getDefault() {
        return new AttunementRequirements(-1, -1, 0, new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("AttunementRequirements: \n" +
                "\txpLevelsThreshold : " + xpLevelThreshold() + "\n" +
                "\txpLevelsConsumed : " + xpLevelsConsumed() + "\n" +
                "\tkills: " + this.kills() + "\n" +
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

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof AttunementRequirements attunementRequirements) {
            return this.xpLevelsConsumed() == attunementRequirements.xpLevelsConsumed() &&
                    this.xpLevelThreshold() == attunementRequirements.xpLevelThreshold() &&
                    this.kills() == attunementRequirements.kills() &&
                    this.items().equals(attunementRequirements.items()) &&
                    this.feats().equals(attunementRequirements.feats());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.xpLevelsConsumed(), this.xpLevelThreshold(), this.kills(), this.items(), this.feats());
    }
}
