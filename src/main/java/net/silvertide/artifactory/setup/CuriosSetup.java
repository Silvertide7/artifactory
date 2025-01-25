package net.silvertide.artifactory.setup;

import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.silvertide.artifactory.compat.CuriosCompat;
import net.silvertide.artifactory.compat.CuriosEvents;
import net.silvertide.artifactory.config.ServerConfigs;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosSetup {
    public static void init(final FMLCommonSetupEvent ignored) {
        if(ModList.get().isLoaded("curios")) {
            CuriosCompat.hasCurios = true;
            NeoForge.EVENT_BUS.addListener(CuriosEvents::onCuriosEquip);
            NeoForge.EVENT_BUS.addListener(CuriosEvents::keepCurios);
        }
    }
}
