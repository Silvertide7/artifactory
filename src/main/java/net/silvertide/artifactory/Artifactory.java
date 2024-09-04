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

// TODO: Need to find a good place to trigger removing item nbt if no longer attuned
// TODO: Create GUI's and textures
// TODO: Create menu / screen for player management of attuned items
// TODO: Curios slots? At least test them
// TODO: Update hovertext to 2 lines - Attuneable (#) / Use Restricted or nothing
// TODO: Text that says "You feel the bond between you and <item display name> fade." When breaking attunement
// TODO: Add a check when the bond is broken to search the players inventory and update the item if it's in there.
// TODO: Add more checks for when the item should be updated if it has been unbonded. On pickup into inventory.
// TODO: If an item loses its attunement data we need to break those items attunements from the players.
// TODO: Change color in GUIs to make it easier to parse
// TODO: Limit it so only items can be attuned in the datapack entry
// TODO: Any time an AttunedItem changes send update to client
// TODO: Have different colored borders around item render on manage screen based on its attunement level compared to the max
// TODO: Add hover text explaining all benefits gained so far.
// TODO: Implement isValidAttunementItem in correct spots. Should items that were valid but become invalid have all data erased?

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
            MenuScreens.register(MenuRegistry.ATTUNEMENT_NEXUS_ATTUNE_MENU.get(), AttunementNexusAttuneScreen::new);
            MinecraftForge.EVENT_BUS.register(new ClientEvents());
        }
    }
}
