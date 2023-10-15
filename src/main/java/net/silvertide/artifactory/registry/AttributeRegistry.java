package net.silvertide.artifactory.registry;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.silvertide.artifactory.Artifactory;

public class AttributeRegistry {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Artifactory.MOD_ID);
    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
    public static final RegistryObject<Attribute> ATTUNEMENT_SLOTS = ATTRIBUTES.register("attunement_slots", () -> new RangedAttribute("attunement_slots", 1.0, 0.0, 100.0).setSyncable(true));
}
