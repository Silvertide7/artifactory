package net.silvertide.artifactory.setup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.silvertide.artifactory.compat.CuriosCompat;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.PlayerMessenger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class CuriosSetup {
    public static void init(final FMLCommonSetupEvent ignored) {
        if(ModList.get().isLoaded("curios")) {

            MinecraftForge.EVENT_BUS.addListener(CuriosCompat::onCuriosEquip);
            MinecraftForge.EVENT_BUS.addListener(CuriosCompat::keepCurios);
            MinecraftForge.EVENT_BUS.addListener(CuriosCompat::onCurioAttributeModifierEvent);

            CuriosApi.registerCurioPredicate(new ResourceLocation("artifactory", "is_attuned_item"), (SlotResult slotResult) -> {
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
