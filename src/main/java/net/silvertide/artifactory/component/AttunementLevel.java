package net.silvertide.artifactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record AttunementLevel(AttunementRequirements requirements, List<String> modifications) {
    public static final Codec<AttunementLevel> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementLevel> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        AttunementRequirements.CODEC.optionalFieldOf("requirements", AttunementRequirements.getDefault()).forGetter(AttunementLevel::requirements),
                        Codec.list(Codec.STRING).optionalFieldOf("modifications", new ArrayList<>()).forGetter(AttunementLevel::modifications))
            .apply(instance, AttunementLevel::new)
        );

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttunementLevel decode(@NotNull RegistryFriendlyByteBuf buf) {
                AttunementRequirements requirements = AttunementRequirements.STREAM_CODEC.decode(buf);

                List<String> modifications = new ArrayList<>();
                int numModifications = buf.readVarInt();
                for(int i = 0; i < numModifications; i++) {
                    modifications.add(buf.readUtf());
                }
                return new AttunementLevel(requirements, modifications);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, AttunementLevel attunementLevel) {
                AttunementRequirements.STREAM_CODEC.encode(buf, attunementLevel.requirements());

                buf.writeVarInt(attunementLevel.modifications().size());
                for(int i = 0; i < attunementLevel.modifications().size(); i++) {
                    buf.writeUtf(attunementLevel.modifications().get(i));
                }
            }
        };
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

    public AttunementLevel withRequirements(AttunementRequirements requirements) {
        return new AttunementLevel(requirements, this.modifications());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("AttunementLevel: \n" +
                " modifications: [");

        for (String modification : modifications) {
            result.append("\t").append(modification).append("\n");
        }

        result.append("requirements:").append(requirements);

        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof AttunementLevel attunementLevel) {
            return this.requirements() == attunementLevel.requirements() &&
                    this.modifications().equals(attunementLevel.modifications());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.requirements(), this.modifications());
    }
}
