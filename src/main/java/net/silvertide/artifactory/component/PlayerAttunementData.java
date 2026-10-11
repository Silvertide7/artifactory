package net.silvertide.artifactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.silvertide.artifactory.config.codecs.CodecTypes;
import net.silvertide.artifactory.modifications.AttributeModification;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record PlayerAttunementData(UUID attunementUUID, UUID attunedToUUID, String attunedToName, boolean isSoulbound, boolean isInvulnerable, boolean isUnbreakable, boolean isResilient, List<AttributeModification> attributeModifications) {
    public static final Codec<PlayerAttunementData> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerAttunementData> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CodecTypes.UUID_CODEC.fieldOf("attunement_uuid").forGetter(PlayerAttunementData::attunementUUID),
                CodecTypes.UUID_CODEC.fieldOf("attuned_to_uuid").forGetter(PlayerAttunementData::attunedToUUID),
                Codec.STRING.fieldOf("attuned_to_name").forGetter(PlayerAttunementData::attunedToName),
                Codec.BOOL.fieldOf("is_soulbound").forGetter(PlayerAttunementData::isSoulbound),
                Codec.BOOL.fieldOf("is_invulnerable").forGetter(PlayerAttunementData::isInvulnerable),
                Codec.BOOL.fieldOf("is_unbreakable").forGetter(PlayerAttunementData::isUnbreakable),
                Codec.BOOL.optionalFieldOf("is_resilient", false).forGetter(PlayerAttunementData::isResilient),
                Codec.list(AttributeModification.CODEC).fieldOf("attribute_modifications").forGetter(PlayerAttunementData::attributeModifications))
            .apply(instance, PlayerAttunementData::new));

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull PlayerAttunementData decode(@NotNull RegistryFriendlyByteBuf buf) {
                UUID attunementUUID = buf.readUUID();
                UUID attunedToUUID = buf.readUUID();
                String attunedToName = buf.readUtf();
                boolean isSoulbound = buf.readBoolean();
                boolean isInvulnerable = buf.readBoolean();
                boolean isUnbreakable = buf.readBoolean();
                boolean isResilient = buf.readBoolean();

                List<AttributeModification> attributeModifications = new ArrayList<>();
                int numModifications = buf.readVarInt();

                for(int i = 0; i < numModifications; i++) {
                    attributeModifications.add(AttributeModification.STREAM_CODEC.decode(buf));
                }

                return new PlayerAttunementData(attunementUUID, attunedToUUID, attunedToName, isSoulbound, isInvulnerable, isUnbreakable, isResilient, attributeModifications);
            }
            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull PlayerAttunementData playerAttunementData) {
                buf.writeUUID(playerAttunementData.attunementUUID());
                buf.writeUUID(playerAttunementData.attunedToUUID());
                buf.writeUtf(playerAttunementData.attunedToName());
                buf.writeBoolean(playerAttunementData.isSoulbound());
                buf.writeBoolean(playerAttunementData.isInvulnerable());
                buf.writeBoolean(playerAttunementData.isUnbreakable());
                buf.writeBoolean(playerAttunementData.isResilient());

                buf.writeVarInt(playerAttunementData.attributeModifications.size());
                for(int i = 0; i < playerAttunementData.attributeModifications.size(); i++) {
                    AttributeModification.STREAM_CODEC.encode(buf, playerAttunementData.attributeModifications().get(i));
                }
            }
        };
    }

    // Builder Methods
    public PlayerAttunementData withIsSoulbound(boolean isSoulbound) {
        return new PlayerAttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                this.attunedToName(),
                isSoulbound,
                this.isInvulnerable(),
                this.isUnbreakable(),
                this.isResilient(),
                this.attributeModifications());
    }

    public PlayerAttunementData withIsInvulnerable(boolean isInvulnerable) {
        return new PlayerAttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                this.attunedToName(),
                this.isSoulbound(),
                isInvulnerable,
                this.isUnbreakable(),
                this.isResilient(),
                this.attributeModifications());
    }

    public PlayerAttunementData withIsUnbreakable(boolean isUnbreakable) {
        return new PlayerAttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                this.attunedToName(),
                this.isSoulbound(),
                this.isInvulnerable(),
                isUnbreakable,
                this.isResilient(),
                this.attributeModifications());
    }

    public PlayerAttunementData withIsResilient(boolean isResilient) {
        return new PlayerAttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                this.attunedToName(),
                this.isSoulbound(),
                this.isInvulnerable(),
                this.isUnbreakable(),
                isResilient,
                this.attributeModifications());
    }

    public PlayerAttunementData withAttributeModifications(List<AttributeModification> attributeModifications) {
        return new PlayerAttunementData(
                this.attunementUUID(),
                this.attunedToUUID(),
                this.attunedToName(),
                this.isSoulbound(),
                this.isInvulnerable(),
                this.isUnbreakable(),
                this.isResilient(),
                attributeModifications);
    }
}
