package net.silvertide.artifactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AttunementOverride(int attunementSlotsUsed, List<AttunementLevel> attunementLevels, double chance, boolean useWithoutAttunement) implements AttunementSchema {
    public static final AttunementOverride NULL_ATTUNEMENT_OVERRIDE = new AttunementOverride(-1, List.of(), 0.0D, true);
    public static final Codec<AttunementOverride> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementOverride> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.fieldOf("slots_used").forGetter(AttunementOverride::attunementSlotsUsed),
                        Codec.list(AttunementLevel.CODEC).fieldOf("attunement_levels").forGetter(AttunementOverride::attunementLevels),
                        Codec.DOUBLE.fieldOf("chance").forGetter(AttunementOverride::chance),
                        Codec.BOOL.fieldOf("use_without_attunement").forGetter(AttunementOverride::useWithoutAttunement))
                .apply(instance, AttunementOverride::new)
        );

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttunementOverride decode(@NotNull RegistryFriendlyByteBuf buf) {
                int attunementSlotsUsed = buf.readInt();

                List<AttunementLevel> attunementLevels = new ArrayList<>();
                int numAttunementLevels = buf.readVarInt();
                for(int i = 0; i < numAttunementLevels; i++) {
                    attunementLevels.add(AttunementLevel.STREAM_CODEC.decode(buf));
                }

                double chance = buf.readDouble();
                boolean useWithoutAttunement = buf.readBoolean();
                return new AttunementOverride(attunementSlotsUsed, attunementLevels, chance, useWithoutAttunement);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull AttunementOverride attunementOverride) {
                buf.writeInt(attunementOverride.attunementSlotsUsed());

                buf.writeVarInt(attunementOverride.attunementLevels().size());
                for(int i = 0; i < attunementOverride.attunementLevels().size(); i++) {
                    AttunementLevel.STREAM_CODEC.encode(buf, attunementOverride.attunementLevels.get(i));
                }

                buf.writeDouble(attunementOverride.chance());
                buf.writeBoolean(attunementOverride.useWithoutAttunement());
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof AttunementOverride(int slotsUsed, List<AttunementLevel> levels, double chanceIn, boolean withoutAttunement)) {
            return this.attunementSlotsUsed() == slotsUsed &&
                    this.useWithoutAttunement() == withoutAttunement &&
                    this.chance() == chanceIn &&
                    this.attunementLevels().equals(levels);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.attunementSlotsUsed(), this.attunementLevels(), this.chance(), this.useWithoutAttunement());
    }
}
