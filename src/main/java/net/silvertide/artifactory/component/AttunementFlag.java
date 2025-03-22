package net.silvertide.artifactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record AttunementFlag(boolean isAttunable) {
    public static final Codec<AttunementFlag> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementFlag> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.BOOL.fieldOf("is_attunable").forGetter(AttunementFlag::isAttunable))
                .apply(instance, AttunementFlag::new));

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttunementFlag decode(@NotNull RegistryFriendlyByteBuf buf) {
                return new AttunementFlag(buf.readBoolean());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, AttunementFlag attunementFlag) {
                buf.writeBoolean(attunementFlag.isAttunable());
            }
        };
    }
}
