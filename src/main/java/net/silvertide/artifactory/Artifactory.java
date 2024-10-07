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
import net.silvertide.artifactory.gui.AttunementNexusAttuneScreen;
import org.slf4j.Logger;

// FUNCTIONAL
// TODO: If a player renames an item after it has been attuned then update the name.
// TODO: Change common config to a server config
// TODO: Store the display name prettified and remove prettify calls

// GUI
// TODO: Add (Max) next to attunement level in manage screen if its maxed out
// TODO: Add current slots used / max slots to manage screen and attune screen.
// TODO: Have different colored borders around item render on manage screen based on its attunement level compared to the max
// TODO If consumed levels is greater than threshold then don't show threshold or show consumed amount
// TODO: Redo block design
// TODO: Redo attune screen gui design

// CLEANUP
// TODO: Remove Curios Tests
// TODO: Setup balanced datapack data

@Mod(Artifactory.MOD_ID)
public class Artifactory
{
    public static final String MOD_ID = "artifactory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public Artifactory() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        AttributeRegistry.register(modEventBus);
        TabRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);

        if (ModList.get().isLoaded("curios")) {
            MinecraftForge.EVENT_BUS.addListener(CuriosCompat::onCuriosEquip);
            MinecraftForge.EVENT_BUS.addListener(CuriosCompat::keepCurios);
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        MinecraftForge.EVENT_BUS.register(this);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {}

    @Mod.EventBusSubscriber(modid = MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(MenuRegistry.ATTUNEMENT_NEXUS_ATTUNE_MENU.get(), AttunementNexusAttuneScreen::new);
            MinecraftForge.EVENT_BUS.register(new ClientEvents());
        }
    }
}
