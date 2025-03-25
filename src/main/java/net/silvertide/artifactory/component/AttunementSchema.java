package net.silvertide.artifactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AttunementSchema(int attunementSlotsUsed, List<AttunementLevel> attunementLevels, boolean useWithoutAttunement, boolean unique) {
    public static final Codec<AttunementSchema> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementSchema> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.fieldOf("slots_used").forGetter(AttunementSchema::attunementSlotsUsed),
                        Codec.list(AttunementLevel.CODEC).fieldOf("attunement_levels").forGetter(AttunementSchema::attunementLevels),
                        Codec.BOOL.fieldOf("use_without_attunement").forGetter(AttunementSchema::useWithoutAttunement),
                        Codec.BOOL.fieldOf("unique").forGetter(AttunementSchema::unique))
                .apply(instance, AttunementSchema::new)
        );

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttunementSchema decode(@NotNull RegistryFriendlyByteBuf buf) {
                int attunementSlotsUsed = buf.readInt();

                List<AttunementLevel> attunementLevels = new ArrayList<>();
                int numAttunementLevels = buf.readVarInt();
                for(int i = 0; i < numAttunementLevels; i++) {
                    attunementLevels.add(AttunementLevel.STREAM_CODEC.decode(buf));
                }

                boolean useWithoutAttunement = buf.readBoolean();
                boolean unique = buf.readBoolean();
                return new AttunementSchema(attunementSlotsUsed, attunementLevels, useWithoutAttunement, unique);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, AttunementSchema attunementSchema) {
                buf.writeInt(attunementSchema.attunementSlotsUsed());

                buf.writeVarInt(attunementSchema.attunementLevels().size());
                for(int i = 0; i < attunementSchema.attunementLevels().size(); i++) {
                    AttunementLevel.STREAM_CODEC.encode(buf, attunementSchema.attunementLevels.get(i));
                }

                buf.writeBoolean(attunementSchema.useWithoutAttunement());
                buf.writeBoolean(attunementSchema.unique());
            }
        };
    }
}
