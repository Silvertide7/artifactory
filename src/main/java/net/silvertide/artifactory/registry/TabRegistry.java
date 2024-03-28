package net.silvertide.artifactory.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.silvertide.artifactory.Artifactory;

public class TabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Artifactory.MOD_ID);

    public static final RegistryObject<CreativeModeTab> COURSE_TAB = CREATIVE_MODE_TABS.register("homebound_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get()))
                    .title(Component.translatable("creativetab.artifactory_tab"))
                    .displayItems((displayParameters, output) -> {
                        // Items
                    output.accept(BlockRegistry.ATTUNEMENT_NEXUS_BLOCK.get());

                    }).build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
