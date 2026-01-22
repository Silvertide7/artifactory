package net.silvertide.artifactory.setup;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CuriosSetup {

    public static void init(final FMLCommonSetupEvent ignored) {
        if(ModList.get().isLoaded("curios")) {
            net.silvertide.artifactory.compat.CuriosCompat.initialize(MinecraftForge.EVENT_BUS);
        }
    }
}
