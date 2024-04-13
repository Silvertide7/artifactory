package net.silvertide.artifactory;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.silvertide.artifactory.compat.CuriosCompat;
import net.silvertide.artifactory.client.events.ClientEvents;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.registry.*;
import net.silvertide.artifactory.gui.AttunementNexusScreen;
import org.slf4j.Logger;

// TODO: Need to find a good place to trigger removing item nbt if no longer attuned
// TODO: Create GUI's and textures
// TODO: Create menu / screen for player management of attuned items
// TODO: Curios slots? At least test them
// TODO: Update hovertext to 2 lines - Attuneable (#) / Use Restricted or nothing
@Mod(Artifactory.MOD_ID)
public class Artifactory
{
    public static final String MOD_ID = "artifactory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public Artifactory()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::commonSetup);

        AttributeRegistry.register(modEventBus);
        TabRegistry.register(modEventBus);

        ItemRegistry.register(modEventBus);

        BlockRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);

        MenuRegistry.register(modEventBus);

        if (ModList.get().isLoaded("curios")) {
            MinecraftForge.EVENT_BUS.addListener(CuriosCompat::onCuriosEquip);
            MinecraftForge.EVENT_BUS.addListener(CuriosCompat::keepCurios);
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

        MinecraftForge.EVENT_BUS.register(this);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {}

    @Mod.EventBusSubscriber(modid = MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(MenuRegistry.ATTUNEMENT_NEXUS_MENU.get(), AttunementNexusScreen::new);
            MinecraftForge.EVENT_BUS.register(new ClientEvents());
        }
    }
}
