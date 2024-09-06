package net.silvertide.artifactory.registry;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.silvertide.artifactory.Artifactory;
@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributeRegistry {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Artifactory.MOD_ID);
    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
    public static final RegistryObject<Attribute> MAX_ATTUNEMENT_SLOTS = ATTRIBUTES.register("max_attunement_slots", () -> new RangedAttribute("attribute.artifactory.max_attunement_slots", 15.0D, 0.0D, 10000.0D).setSyncable(true));

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> {
            e.add(entity, MAX_ATTUNEMENT_SLOTS.get());
        });
    }
}
