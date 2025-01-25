package net.silvertide.artifactory.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.silvertide.artifactory.util.*;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosEvents {
    // Check if the curios item should not be equipable and prevent it if so.
    @SubscribeEvent
    public static void onCuriosEquip(CurioCanEquipEvent event) {
        SlotContext slotContext = event.getSlotContext();

        if(slotContext.entity() instanceof ServerPlayer serverPlayer) {
            ItemStack stack = event.getStack();
            AttunementService.clearBrokenAttunementIfExists(stack);

            if(AttunementUtil.isValidAttunementItem(stack)) {
                if(AttunementUtil.isAttunedToAnotherPlayer(serverPlayer, stack)) {
                    PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.owned_by_another_player");
                    event.setEquipResult(TriState.FALSE);
                } else if (!AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack) && !DataPackUtil.canUseWithoutAttunement(stack)) {
                    PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.item_not_equippable");
                    event.setEquipResult(TriState.FALSE);
                } else if (slotContext.identifier().equals("artifactory")) {
                    if(!AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack)) {
                        PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.item_not_equippable");
                        event.setEquipResult(TriState.FALSE);
                    } else {
                        event.setEquipResult(TriState.TRUE);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void keepCurios(DropRulesEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CuriosApi.getCuriosInventory(serverPlayer).ifPresent(itemHandler -> {
                for (int i = 0; i < itemHandler.getSlots(); ++i) {
                    int finalI = i;
                    ItemStack curiosStack = itemHandler.getEquippedCurios().getStackInSlot(finalI);
                    if(AttunementUtil.isSoulboundActive(serverPlayer, curiosStack)) {
                        event.addOverride(stack -> stack == itemHandler.getEquippedCurios().getStackInSlot(finalI), ICurio.DropRule.ALWAYS_KEEP);
                    }
                }
            });
        }
    }

//    @SubscribeEvent
//    public static void onCurioAttributeModifierEvent(CurioAttributeModifierEvent event) {
//        ItemStack stack = event.getItemStack();
//        if (AttunementUtil.isValidAttunementItem(stack) && StackNBTUtil.containsAttributeModifications(stack)) {
//            CompoundTag artifactoryAttributeModificationsTag = StackNBTUtil.getOrCreateAttributeModificationNBT(stack);
//            for(String attributeModificationKey : artifactoryAttributeModificationsTag.getAllKeys()) {
//                AttributeModification.fromCompoundTag(artifactoryAttributeModificationsTag.getCompound(attributeModificationKey)).ifPresent(attributeModification -> {
//                    attributeModification.addCurioAttributeModifier(event);
//                });
//            }
//        }
//    }
}
