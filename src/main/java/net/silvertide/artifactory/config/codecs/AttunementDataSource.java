package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.silvertide.artifactory.component.AttunementLevel;
import net.silvertide.artifactory.component.AttunementSchema;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AttunementDataSource(int attunementSlotsUsed, List<AttunementLevel> attunementLevels, double chance, boolean useWithoutAttunement, boolean replace) implements AttunementSchema {
    public static final Codec<AttunementDataSource> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementDataSource> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.optionalFieldOf("slots_used", -1).forGetter(AttunementDataSource::attunementSlotsUsed),
                        Codec.list(AttunementLevel.CODEC).optionalFieldOf("attunement_levels", List.of()).forGetter(AttunementDataSource::attunementLevels),
                        Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(AttunementDataSource::chance),
                        Codec.BOOL.optionalFieldOf("use_without_attunement", true).forGetter(AttunementDataSource::useWithoutAttunement),
                        Codec.BOOL.optionalFieldOf("replace", false).forGetter(AttunementDataSource::replace))
                .apply(instance, AttunementDataSource::new)
        );

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttunementDataSource decode(@NotNull RegistryFriendlyByteBuf buf) {
                int attunementSlotsUsed = buf.readInt();

                List<AttunementLevel> attunementLevels = new ArrayList<>();
                int numAttunementLevels = buf.readVarInt();
                for(int i = 0; i < numAttunementLevels; i++) {
                    attunementLevels.add(AttunementLevel.STREAM_CODEC.decode(buf));
                }

                double chance = buf.readDouble();
                boolean useWithoutAttunement = buf.readBoolean();
                boolean replace = buf.readBoolean();
                return new AttunementDataSource(attunementSlotsUsed, attunementLevels, chance, useWithoutAttunement, replace);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, AttunementDataSource attunementDataSource) {
                buf.writeInt(attunementDataSource.attunementSlotsUsed());

                buf.writeVarInt(attunementDataSource.attunementLevels().size());
                for(int i = 0; i < attunementDataSource.attunementLevels().size(); i++) {
                    AttunementLevel.STREAM_CODEC.encode(buf, attunementDataSource.attunementLevels.get(i));
                }

                buf.writeDouble(attunementDataSource.chance());
                buf.writeBoolean(attunementDataSource.useWithoutAttunement());
                buf.writeBoolean(attunementDataSource.replace());
            }
        };
    }

    public AttunementDataSource withAttunementLevels(List<AttunementLevel> attunementLevels) {
        return new AttunementDataSource(this.attunementSlotsUsed(), attunementLevels, this.chance(), this.useWithoutAttunement(), this.replace());
    }

    public AttunementDataSource withChance(double chance) {
        return new AttunementDataSource(this.attunementSlotsUsed(), this.attunementLevels(), chance, this.useWithoutAttunement(), this.replace());
    }

//    public String toString() {
//        StringBuilder attunementString = new StringBuilder();
//
//        for(int i = 0; i < attunementLevels().size(); i++) {
//            attunementString.append("Level ").append(i + 1).append(": ").append(attunementLevels().get(i)).append(": \n");
//
//        }
//
//        return "replace: " + replace() + "\n" +
//                "attunement_slots_used: " + getAttunementSlotsUsed() + "\n" +
//                "use_without_attunement: " + useWithoutAttunement() + "\n" +
//                "unique: " + unique() + "\n" +
//                attunementString;
//    }
}
