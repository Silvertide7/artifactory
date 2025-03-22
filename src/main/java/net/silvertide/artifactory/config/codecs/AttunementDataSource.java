package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.silvertide.artifactory.modifications.AttributeModification;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record AttunementDataSource(int attunementSlotsUsed, List<AttunementLevel> attunementLevels, boolean useWithoutAttunement, boolean unique, boolean replace) {
    public static final Codec<AttunementDataSource> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementDataSource> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.optionalFieldOf("slots_used", -1).forGetter(AttunementDataSource::attunementSlotsUsed),
                        Codec.list(AttunementLevel.CODEC).optionalFieldOf("attunement_levels", getDefaultAttunementLevels()).forGetter(AttunementDataSource::attunementLevels),
                        Codec.BOOL.optionalFieldOf("use_without_attunement", true).forGetter(AttunementDataSource::useWithoutAttunement),
                        Codec.BOOL.optionalFieldOf("unique", false).forGetter(AttunementDataSource::unique),
                        Codec.BOOL.optionalFieldOf("replace", false).forGetter(AttunementDataSource::replace))
                .apply(instance, AttunementDataSource::new)
        );

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttunementDataSource decode(@NotNull RegistryFriendlyByteBuf buf) {
                UUID attunementUUID = buf.readUUID();
                UUID attunedToUUID = buf.readUUID();
                String attunedToName = buf.readUtf();
                boolean isSoulbound = buf.readBoolean();
                boolean isInvulnerable = buf.readBoolean();
                boolean isUnbreakable = buf.readBoolean();

                List<AttributeModification> attributeModifications = new ArrayList<>();
                int numModifications = buf.readVarInt();

                for(int i = 0; i < numModifications; i++) {
                    attributeModifications.add(AttributeModification.STREAM_CODEC.decode(buf));
                }

                return new AttunementDataSource(attunementUUID, attunedToUUID, attunedToName, isSoulbound, isInvulnerable, isUnbreakable, attributeModifications);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, AttunementDataSource attunementData) {
                buf.writeUUID(attunementData.attunementUUID());
                buf.writeUUID(attunementData.attunedToUUID());
                buf.writeUtf(attunementData.attunedToName());
                buf.writeBoolean(attunementData.isSoulbound());
                buf.writeBoolean(attunementData.isInvulnerable());
                buf.writeBoolean(attunementData.isUnbreakable());

                buf.writeVarInt(attunementData.attributeModifications.size());
                for(int i = 0; i < attunementData.attributeModifications.size(); i++) {
                    AttributeModification.STREAM_CODEC.encode(buf, attunementData.attributeModifications().get(i));
                }
            }
        };
    }


    public int getAttunementSlotsUsed() {
        if(attunementSlotsUsed < 0) return 0;
        return attunementSlotsUsed;
    }

    private static List<AttunementLevel> getDefaultAttunementLevels() {
        return List.of(AttunementLevel.getDefault());
    }

    public String toString() {
        StringBuilder attunementString = new StringBuilder();

        for(int i = 0; i < attunementLevels().size(); i++) {
            attunementString.append("Level ").append(i + 1).append(": ").append(attunementLevels().get(i)).append(": \n");

        }

        return "replace: " + replace() + "\n" +
                "attunement_slots_used: " + getAttunementSlotsUsed() + "\n" +
                "use_without_attunement: " + useWithoutAttunement() + "\n" +
                "unique: " + unique() + "\n" +
                attunementString;
    }
}
