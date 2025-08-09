package net.silvertide.artifactory.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.silvertide.artifactory.Artifactory;
@EventBusSubscriber(modid = Artifactory.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class AttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Artifactory.MOD_ID);
    public static void register(IEventBus eventBus) { ATTRIBUTES.register(eventBus); }

    public static final DeferredHolder<Attribute, Attribute> ATTUNEMENT_SLOTS = ATTRIBUTES.register("attunement_slots", () ->
            new RangedAttribute("attribute.artifactory.max_attunement_slots", 20, 0, Integer.MAX_VALUE).setSyncable(true));


    @SubscribeEvent
    public static void addAttributes(final EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ATTUNEMENT_SLOTS);
    }
}
