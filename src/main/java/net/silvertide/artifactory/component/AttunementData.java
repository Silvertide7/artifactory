package net.silvertide.artifactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.silvertide.artifactory.config.codecs.CodecTypes;
import net.silvertide.artifactory.modifications.AttributeModification;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record AttunementData(UUID attunementUUID, UUID attunedToUUID, String attunedToName, boolean isSoulbound, boolean isInvulnerable, boolean isUnbreakable, List<AttributeModification> attributeModifications) {
    public static final Codec<AttunementData> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementData> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CodecTypes.UUID_CODEC.fieldOf("attunement_uuid").forGetter(AttunementData::attunementUUID),
                CodecTypes.UUID_CODEC.fieldOf("attuned_to_uuid").forGetter(AttunementData::attunedToUUID),
                Codec.STRING.fieldOf("attuned_to_name").forGetter(AttunementData::attunedToName),
                Codec.BOOL.fieldOf("is_soulbound").forGetter(AttunementData::isSoulbound),
                Codec.BOOL.fieldOf("is_invulnerable").forGetter(AttunementData::isInvulnerable),
                Codec.BOOL.fieldOf("is_unbreakable").forGetter(AttunementData::isUnbreakable),
                Codec.list(AttributeModification.CODEC).fieldOf("attribute_modifications").forGetter(AttunementData::attributeModifications))
            .apply(instance, AttunementData::new));

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttunementData decode(@NotNull RegistryFriendlyByteBuf buf) {
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

                return new AttunementData(attunementUUID, attunedToUUID, attunedToName, isSoulbound, isInvulnerable, isUnbreakable, attributeModifications);
            }
            @Override
            public void encode(RegistryFriendlyByteBuf buf, AttunementData attunementData) {
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

    // Builder Methods
    public AttunementData withAttunedToUUID(UUID attunedToUUID) {
        return new AttunementData(
                this.attunementUUID(),
                attunedToUUID,
                this.attunedToName(),
                this.isSoulbound(),
                this.isInvulnerable(),
                this.isUnbreakable(),
                this.attributeModifications());
    }

    public AttunementData withAttunedToName(String attunedToName) {
        return new AttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                attunedToName,
                this.isSoulbound(),
                this.isInvulnerable(),
                this.isUnbreakable(),
                this.attributeModifications());
    }

    public AttunementData withIsSoulbound(boolean isSoulbound) {
        return new AttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                this.attunedToName(),
                isSoulbound,
                this.isInvulnerable(),
                this.isUnbreakable(),
                this.attributeModifications());
    }

    public AttunementData withIsInvulnerable(boolean isInvulnerable) {
        return new AttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                this.attunedToName(),
                this.isSoulbound(),
                isInvulnerable,
                this.isUnbreakable(),
                this.attributeModifications());
    }

    public AttunementData withIsUnbreakable(boolean isUnbreakable) {
        return new AttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                this.attunedToName(),
                this.isSoulbound(),
                this.isInvulnerable(),
                isUnbreakable,
                this.attributeModifications());
    }

    public AttunementData withAttributeModifications(List<AttributeModification> attributeModifications) {
        return new AttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                this.attunedToName(),
                this.isSoulbound(),
                this.isInvulnerable(),
                this.isUnbreakable(),
                attributeModifications);
    }

    public boolean hasAttributeModifications() {
        return !attributeModifications().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttunementData other = (AttunementData) o;
        return isSoulbound() == other.isSoulbound()
                && isInvulnerable() == other.isInvulnerable()
                && isUnbreakable() == other.isUnbreakable()
                && Objects.equals(attunementUUID, other.attunementUUID)
                && Objects.equals(attunedToUUID, other.attunedToUUID)
                && Objects.equals(attunedToName, other.attunedToName)
                && Objects.equals(attributeModifications, other.attributeModifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attunementUUID, attunedToUUID, attunedToName, isSoulbound(), isInvulnerable(), isUnbreakable(), attributeModifications);
    }
}
