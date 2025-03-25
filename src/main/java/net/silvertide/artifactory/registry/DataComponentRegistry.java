package net.silvertide.artifactory.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.component.AttunementFlag;

import java.util.function.UnaryOperator;

public class DataComponentRegistry {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Artifactory.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AttunementSchema>> ATTUNEMENT_SCHEMA = register("attunement_schema",
            builder -> builder.persistent(AttunementSchema.CODEC).networkSynchronized(AttunementSchema.STREAM_CODEC));


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlayerAttunementData>> PLAYER_ATTUNEMENT_DATA = register("attunement_data",
            builder -> builder.persistent(PlayerAttunementData.CODEC).networkSynchronized(PlayerAttunementData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AttunementFlag>> ATTUNEMENT_FLAG = register("attunement_flag",
            builder -> builder.persistent(AttunementFlag.CODEC).networkSynchronized(AttunementFlag.STREAM_CODEC));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
                                                                                           UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }
    public static void register(IEventBus eventBus) { DATA_COMPONENT_TYPES.register(eventBus); }
}
