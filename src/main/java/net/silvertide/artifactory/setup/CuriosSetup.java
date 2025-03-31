package net.silvertide.artifactory.setup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.silvertide.artifactory.compat.CuriosEvents;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.services.PlayerMessenger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class CuriosSetup {
    public static void init(final FMLCommonSetupEvent ignored) {
        if(ModList.get().isLoaded("curios")) {
            NeoForge.EVENT_BUS.addListener(CuriosEvents::onCuriosEquip);
            NeoForge.EVENT_BUS.addListener(CuriosEvents::keepCurios);
            NeoForge.EVENT_BUS.addListener(CuriosEvents::onCurioAttributeModifierEvent);

            CuriosApi.registerCurioPredicate(ResourceLocation.fromNamespaceAndPath("artifactory", "is_attuned_item"), (SlotResult slotResult) -> {
                if(slotResult.slotContext().entity() instanceof ServerPlayer serverPlayer) {
                    ItemStack stack = slotResult.stack();
                    if(AttunementUtil.isValidAttunementItem(stack)) {
                        if(!AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack)) {
                            PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.item_not_equippable");
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
                return false;
            });
        }
    }
}
