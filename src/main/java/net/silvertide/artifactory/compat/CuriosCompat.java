package net.silvertide.artifactory.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.PlayerMessenger;
import net.silvertide.artifactory.util.StackNBTUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosCompat {

    @SubscribeEvent
    public static void onCuriosEquip(CurioEquipEvent event) {
        SlotContext slotContext = event.getSlotContext();

        if(!event.isCanceled() && slotContext.entity() instanceof Player) {
            if(slotContext.entity() instanceof Player player && !player.level().isClientSide()){
                ItemStack stack = event.getStack();
                if(AttunementUtil.isAttunementItem(stack) && !DataPackUtil.getAttunementData(stack).map(ItemAttunementData::useWithoutAttunement).orElse(false)) {
                    PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.item_not_equippable");
                    event.setResult(Event.Result.DENY);
                }
            }
        } else {
            event.setResult(Event.Result.DEFAULT);
        }
    }

    @SubscribeEvent
    public static void keepCurios(DropRulesEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!player.level().isClientSide()) {
                changeSoulboundCuriosDropRule(player, event);
            }
        }
    }

    public static void changeSoulboundCuriosDropRule(Player player, DropRulesEvent event) {
        CuriosApi.getCuriosInventory(player).ifPresent(itemHandler -> {
            for (int i = 0; i < itemHandler.getSlots(); ++i) {
                int finalI = i;
                ItemStack curiosStack = itemHandler.getEquippedCurios().getStackInSlot(finalI);
                if(!curiosStack.isEmpty() && StackNBTUtil.isSoulbound(curiosStack)) {
                    event.addOverride(stack -> stack == itemHandler.getEquippedCurios().getStackInSlot(finalI), ICurio.DropRule.ALWAYS_KEEP);
                }
            }
        });
    }

}
