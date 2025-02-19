package net.silvertide.artifactory;

import com.mojang.logging.LogUtils;
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
import net.silvertide.artifactory.client.events.TooltipHandler;
import net.silvertide.artifactory.compat.CuriosCompat;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.registry.*;
import net.silvertide.artifactory.setup.CuriosSetup;
import org.slf4j.Logger;

// Future Features
// TODO: Make a keybind that searches nearby players and pulls your items into your inventory.
// TODO: Search through all containers if theyre in the inventory as well, like backpacks, for soulbound.
// TODO: Add curios slots that only allow artifacts placed in them for storage.

@Mod(Artifactory.MOD_ID)
public class Artifactory
{
    public static final String MOD_ID = "artifactory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public Artifactory() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        AttributeRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);

        modEventBus.addListener(CuriosSetup::init);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        MinecraftForge.EVENT_BUS.register(this);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {}

    @Mod.EventBusSubscriber(modid = MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            net.minecraft.client.gui.screens.MenuScreens.register(MenuRegistry.ATTUNEMENT_NEXUS_ATTUNE_MENU.get(), net.silvertide.artifactory.gui.AttunementScreen::new);
            MinecraftForge.EVENT_BUS.register(new TooltipHandler());
        }
    }
}
