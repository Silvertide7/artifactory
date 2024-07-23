package net.silvertide.artifactory.registry;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.gui.AttunementNexusAttuneMenu;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Artifactory.MOD_ID);

    public static final RegistryObject<MenuType<AttunementNexusAttuneMenu>> ATTUNEMENT_NEXUS_ATTUNE_MENU = registerMenuType("attunement_nexus_attune_menu", AttunementNexusAttuneMenu::new);

    public static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
