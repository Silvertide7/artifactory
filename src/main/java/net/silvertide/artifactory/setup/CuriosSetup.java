package net.silvertide.artifactory.setup;

import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

public class CuriosSetup {
    public static void init(final FMLCommonSetupEvent ignored) {
        if(ModList.get().isLoaded("curios")) {
            net.silvertide.artifactory.compat.CuriosCompat.initialize(NeoForge.EVENT_BUS);
        }
    }
}
