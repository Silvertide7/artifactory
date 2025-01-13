package net.silvertide.artifactory.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.silvertide.artifactory.modifications.AttributeModification;

import java.util.List;
import java.util.UUID;

//  This was taken from Project MMO CodecTypes
//  https://github.com/Caltinor/Project-MMO-2.0/blob/main/src/main/java/harmonised/pmmo/config/codecs/CodecTypes.java
public class CodecTypes {
    public static final PrimitiveCodec<UUID> UUID_CODEC = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<UUID> read(DynamicOps<T> ops, T input) {
            return DataResult.success(UUID.fromString(ops.getStringValue(input).getOrThrow()));
        }
        @Override
        public <T> T write(DynamicOps<T> ops, UUID value) {
            return ops.createString(value.toString());
        }
        @Override
        public String toString() { return "uuid";}
    };

    public static final Codec<List<AttributeModification>> ATTRIBUTE_MODIFICATION_CODEC = AttributeModification.CODEC.listOf();
}


