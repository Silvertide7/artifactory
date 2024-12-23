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
import net.silvertide.artifactory.client.events.ClientSetupEvents;
import net.silvertide.artifactory.compat.CuriosCompat;
import net.silvertide.artifactory.config.Config;
import net.silvertide.artifactory.registry.*;
import org.slf4j.Logger;

// Playtest Notes
// TODO: DONE - TEST THIS THOUGH -Holding another players weapon allows you to damage mobs.
// TODO: DONE - TEST - Move this item is attuned to <name> to the requirement text instead.
// TODO: Done - Test Don't render the item renderers if the item is attuned by another person.
// TODO: Done - Test When placing a unique attuned item into attunement nexus the text shows this item is attuned to %1$ in required field.
// TODO: Done - Test Client side when breaking a block it shows the block break and then reform. Prevent it entirely.
// TODO: Poison isn't doing damage to player holding another players item.
// TODO: Done - testMake sure soulbound only applies to a player who owns the item.
// TODO: Done -  If an item is attuned and not unique, then changed to unique it breaks things. Not really it just has the wrong messages.

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
            net.minecraft.client.gui.screens.MenuScreens.register(MenuRegistry.ATTUNEMENT_NEXUS_ATTUNE_MENU.get(), net.silvertide.artifactory.gui.AttunementScreen::new);
            MinecraftForge.EVENT_BUS.register(new ClientSetupEvents());
        }
    }
}
