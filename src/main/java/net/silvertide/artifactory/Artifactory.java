package net.silvertide.artifactory;

import com.mojang.logging.LogUtils;

import net.minecraft.network.chat.Component;
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

// TODO: Test curios with attuned items from other players - remove curios implementation gradle
// TODO: Test to make sure attuned items are only usable in any way by their wielder

// TODO: Playtest
// TODO: When breaking a block it shows the block broken then denies it

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

        modEventBus.addListener(CuriosSetup::init);
        modEventBus.addListener(this::addPackFinders);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s-server.toml", Artifactory.MOD_ID));
    }


    public void addPackFinders(AddPackFindersEvent event) {
        Artifactory.LOGGER.debug("addPackFinders");
        try {
            if (event.getPackType() == PackType.SERVER_DATA) {
                addBuiltinPack(event, "artifactory_default_data_pack", Component.literal("Artifactory Defaults"));
            }
        } catch (IOException ex) {
            Artifactory.LOGGER.error("Failed to load a builtin data pack! If you are seeing this message, please report an issue to https://github.com/Silvertide7/artifactory/issues");
        }
    }

    private static void addBuiltinPack(AddPackFindersEvent event, String filename, Component displayName) throws IOException {
        filename = "builtin_data_packs/" + filename;
        String id = "builtin/" + filename;
        var resourcePath = ModList.get().getModFileById(Artifactory.MOD_ID).getFile().findResource(filename);
        var pack = Pack.readMetaAndCreate(
                new PackLocationInfo(id, displayName, PackSource.BUILT_IN, Optional.empty()),
                BuiltInPackSource.fromName((path) -> new PathPackResources(path, resourcePath)),
                PackType.SERVER_DATA,
                new PackSelectionConfig(false, Pack.Position.TOP, false)
        );
        event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
    }


}
