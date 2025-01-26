package net.silvertide.artifactory.setup;

import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.silvertide.artifactory.compat.CuriosEvents;

public class CuriosSetup {
    public static void init(final FMLCommonSetupEvent ignored) {
        if(ModList.get().isLoaded("curios")) {
            NeoForge.EVENT_BUS.addListener(CuriosEvents::onCuriosEquip);
            NeoForge.EVENT_BUS.addListener(CuriosEvents::keepCurios);
            NeoForge.EVENT_BUS.addListener(CuriosEvents::onCurioAttributeModifierEvent);
        }
    }
}
