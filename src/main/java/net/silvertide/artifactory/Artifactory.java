package net.silvertide.artifactory;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.silvertide.artifactory.compat.CuriosCompat;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.registry.*;
import org.slf4j.Logger;

// TODO: Get curios working
// TODO: Add curios specific slots for artifact storage
@Mod(Artifactory.MOD_ID)
public class Artifactory
{
    public static final String MOD_ID = "artifactory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public Artifactory(IEventBus modEventBus, ModContainer modContainer) {
        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        AttributeRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);
        DataComponentRegistry.register(modEventBus);

        if (ModList.get().isLoaded("curios")) {
            NeoForge.EVENT_BUS.addListener(CuriosCompat::onCuriosEquip);
            NeoForge.EVENT_BUS.addListener(CuriosCompat::keepCurios);
        }
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", Artifactory.MOD_ID));
    }
}
