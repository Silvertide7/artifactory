package net.silvertide.artifactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record AttunementFlag(boolean isAttunable, boolean discovered, double chance) {
    public static final Codec<AttunementFlag> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementFlag> STREAM_CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.BOOL.fieldOf("is_attunable").forGetter(AttunementFlag::isAttunable),
                        Codec.BOOL.fieldOf("discovered").forGetter(AttunementFlag::discovered),
                        Codec.DOUBLE.fieldOf("chance").forGetter(AttunementFlag::chance))
                .apply(instance, AttunementFlag::new));

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull AttunementFlag decode(@NotNull RegistryFriendlyByteBuf buf) {
                return new AttunementFlag(buf.readBoolean(), buf.readBoolean(), buf.readDouble());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, AttunementFlag attunementFlag) {
                buf.writeBoolean(attunementFlag.isAttunable());
                buf.writeBoolean(attunementFlag.discovered());
                buf.writeDouble(attunementFlag.chance());
            }
        };
    }

    public static AttunementFlag getAttunableFlag() {
        return new AttunementFlag(true, true, 1.0D);
    }

    public static AttunementFlag getNonAttunableFlag() {
        return new AttunementFlag(false, true, 0.0D);
    }
}
