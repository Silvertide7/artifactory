package net.silvertide.artifactory;

import com.mojang.logging.LogUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.silvertide.artifactory.config.ServerConfigs;
import net.silvertide.artifactory.registry.*;
import net.silvertide.artifactory.setup.CuriosSetup;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;

// TODO: Playtest
// TODO: Test overrides - So far so good
// TODO: Implement AddPack from discord
// Clean up files
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

        modEventBus.addListener(this::addPackFinders);
        modEventBus.addListener(CuriosSetup::init);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", Artifactory.MOD_ID));
    }

    public void addPackFinders(AddPackFindersEvent event) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Artifactory.MOD_ID, "builtin_data_packs/artifactory_default_data_pack");
            event.addPackFinders(location, PackType.SERVER_DATA,Component.literal("Artifactory Defaults"), PackSource.FEATURE,false, Pack.Position.TOP);
    }
}
