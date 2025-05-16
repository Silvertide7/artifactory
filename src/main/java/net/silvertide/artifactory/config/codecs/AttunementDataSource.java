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

public record AttunementDataSource(int attunementSlotsUsed, List<AttunementLevel> attunementLevels, double chance, boolean useWithoutAttunement, List<String> applyToItems,  boolean replace) implements AttunementSchema {
    public static final Codec<AttunementDataSource> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementDataSource> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.optionalFieldOf("slots_used", -1).forGetter(AttunementDataSource::attunementSlotsUsed),
                        Codec.list(AttunementLevel.CODEC).optionalFieldOf("attunement_levels", List.of()).forGetter(AttunementDataSource::attunementLevels),
                        Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(AttunementDataSource::chance),
                        Codec.BOOL.optionalFieldOf("use_without_attunement", true).forGetter(AttunementDataSource::useWithoutAttunement),
                        Codec.list(Codec.STRING).optionalFieldOf("apply_to_items", List.of()).forGetter(AttunementDataSource::applyToItems),
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

                List<String> applyToItems = new ArrayList<>();
                int numApplyToItems = buf.readVarInt();
                for(int i = 0; i < numApplyToItems; i++) {
                    applyToItems.add(buf.readUtf());
                }

                boolean replace = buf.readBoolean();
                return new AttunementDataSource(attunementSlotsUsed, attunementLevels, chance, useWithoutAttunement, applyToItems, replace);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull AttunementDataSource attunementDataSource) {
                buf.writeInt(attunementDataSource.attunementSlotsUsed());

                buf.writeVarInt(attunementDataSource.attunementLevels().size());
                for(int i = 0; i < attunementDataSource.attunementLevels().size(); i++) {
                    AttunementLevel.STREAM_CODEC.encode(buf, attunementDataSource.attunementLevels.get(i));
                }

                buf.writeDouble(attunementDataSource.chance());
                buf.writeBoolean(attunementDataSource.useWithoutAttunement());

                buf.writeVarInt(attunementDataSource.applyToItems().size());
                for(int i = 0; i < attunementDataSource.applyToItems().size(); i++) {
                    buf.writeUtf(attunementDataSource.applyToItems.get(i));
                }

                buf.writeBoolean(attunementDataSource.replace());
            }
        };
    }

    public AttunementDataSource withAttunementLevels(List<AttunementLevel> attunementLevels) {
        return new AttunementDataSource(this.attunementSlotsUsed(), attunementLevels, this.chance(), this.useWithoutAttunement(), this.applyToItems, this.replace());
    }

    public AttunementDataSource withChance(double chance) {
        return new AttunementDataSource(this.attunementSlotsUsed(), this.attunementLevels(), chance, this.useWithoutAttunement(), this.applyToItems, this.replace());
    }
}
