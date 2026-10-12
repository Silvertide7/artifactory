package net.silvertide.artifactory.component.condition;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.silvertide.artifactory.Artifactory;

import java.util.List;
import java.util.Map;

public final class AttunementConditions {
    private static final Map<String, MapCodec<? extends AttunementCondition>> CONDITION_CODECS_BY_TYPE = Map.of(
            ComponentIntRangeCondition.TYPE, ComponentIntRangeCondition.MAP_CODEC,
            ComponentPresentCondition.TYPE, ComponentPresentCondition.MAP_CODEC,
            ComponentEqualsCondition.TYPE, ComponentEqualsCondition.MAP_CODEC,
            UnreadableAttunementCondition.TYPE, UnreadableAttunementCondition.MAP_CODEC);

    private static final Codec<String> CONDITION_TYPE_CODEC = Codec.STRING.validate(type -> CONDITION_CODECS_BY_TYPE.containsKey(type)
            ? DataResult.success(type)
            : DataResult.error(() -> "Unknown attunement condition type " + type));

    private static final Codec<AttunementCondition> CONDITION_CODEC = CONDITION_TYPE_CODEC.dispatch("type", AttunementCondition::type, CONDITION_CODECS_BY_TYPE::get);

    private static final Codec<List<AttunementCondition>> STRICT_LIST_CODEC = CONDITION_CODEC.listOf();

    public static final Codec<List<AttunementCondition>> LIST_CODEC_FAILING_CLOSED = new Codec<>() {
        @Override
        public <T> DataResult<Pair<List<AttunementCondition>, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<List<AttunementCondition>, T>> decoded = STRICT_LIST_CODEC.decode(ops, input);
            return decoded.error()
                    .<DataResult<Pair<List<AttunementCondition>, T>>>map(error -> DataResult.error(error::message, Pair.of(List.of(UnreadableAttunementCondition.INSTANCE), input)))
                    .orElse(decoded);
        }

        @Override
        public <T> DataResult<T> encode(List<AttunementCondition> conditions, DynamicOps<T> ops, T prefix) {
            return STRICT_LIST_CODEC.encode(conditions, ops, prefix);
        }
    };

    private static final int MAX_SYNCED_CONDITIONS_JSON_LENGTH = 262144;

    public static final StreamCodec<RegistryFriendlyByteBuf, List<AttunementCondition>> LIST_STREAM_CODEC = StreamCodec.of(
            (buf, conditions) -> buf.writeUtf(STRICT_LIST_CODEC.encodeStart(buf.registryAccess().createSerializationContext(JsonOps.INSTANCE), conditions).getOrThrow().toString(), MAX_SYNCED_CONDITIONS_JSON_LENGTH),
            buf -> decodeSyncedConditionsFailingClosed(buf, buf.readUtf(MAX_SYNCED_CONDITIONS_JSON_LENGTH)));

    private static List<AttunementCondition> decodeSyncedConditionsFailingClosed(RegistryFriendlyByteBuf buf, String conditionsJson) {
        DataResult<List<AttunementCondition>> decoded = STRICT_LIST_CODEC.parse(buf.registryAccess().createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(conditionsJson));
        decoded.error().ifPresent(error -> Artifactory.LOGGER.error("Artifactory - could not read synced attunement conditions: {}", error.message()));
        return decoded.error().isPresent() ? List.of(UnreadableAttunementCondition.INSTANCE) : decoded.getOrThrow();
    }

    public static boolean isUnreadable(List<AttunementCondition> conditions) {
        return conditions.contains(UnreadableAttunementCondition.INSTANCE);
    }

    private AttunementConditions() {}
}
