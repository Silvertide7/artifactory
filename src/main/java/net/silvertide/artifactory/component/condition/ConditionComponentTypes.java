package net.silvertide.artifactory.component.condition;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.silvertide.artifactory.Artifactory;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class ConditionComponentTypes {
    private static final String VANILLA_NAMESPACE = "minecraft";
    private static final Set<ResourceLocation> COMPONENTS_REPORTED_AS_UNREGISTERED = ConcurrentHashMap.newKeySet();
    private static final Set<String> UNREADABLE_COMPONENTS_REPORTED_BY_CONDITION_TYPE = ConcurrentHashMap.newKeySet();

    private ConditionComponentTypes() {}

    sealed interface Lookup permits Found, OwningModAbsent, MissingFromLoadedMod {}
    record Found(DataComponentType<?> componentType) implements Lookup {}
    record OwningModAbsent() implements Lookup {}
    record MissingFromLoadedMod() implements Lookup {}

    static Lookup lookUp(ResourceLocation component) {
        DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(component);
        if (componentType != null) return new Found(componentType);
        boolean owningModLoaded = VANILLA_NAMESPACE.equals(component.getNamespace()) || ModList.get().isLoaded(component.getNamespace());
        if (COMPONENTS_REPORTED_AS_UNREGISTERED.add(component)) {
            if (owningModLoaded) {
                Artifactory.LOGGER.warn("Artifactory - attunement condition component {} does not exist although {} is loaded (typo or unsupported version?), treating the condition as failed", component, component.getNamespace());
            } else {
                Artifactory.LOGGER.debug("Artifactory - attunement condition component {} belongs to a mod that is not installed, treating the condition as met", component);
            }
        }
        return owningModLoaded ? new MissingFromLoadedMod() : new OwningModAbsent();
    }

    static void reportUnreadableOnce(String conditionType, ResourceLocation component, String expectation) {
        if (UNREADABLE_COMPONENTS_REPORTED_BY_CONDITION_TYPE.add(conditionType + " " + component)) {
            Artifactory.LOGGER.warn("Artifactory - {} condition on component {} {}, treating the condition as failed", conditionType, component, expectation);
        }
    }
}
