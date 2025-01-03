package net.silvertide.artifactory.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.silvertide.artifactory.modifications.AttributeModification;
import net.silvertide.artifactory.util.*;

public class CuriosCompat {
//
//    @SubscribeEvent
//    public static void onCuriosEquip(CurioEquipEvent event) {
//        SlotContext slotContext = event.getSlotContext();
//
//        if(!event.isCanceled() && slotContext.entity() instanceof Player) {
//            if(slotContext.entity() instanceof Player player && !player.level().isClientSide()) {
//                ItemStack stack = event.getStack();
//                AttunementService.clearBrokenAttunementIfExists(stack);
//
//                if(AttunementUtil.isValidAttunementItem(stack)) {
//                    if(AttunementUtil.isAttunedToAnotherPlayer(player, stack)) {
//                        PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.owned_by_another_player");
//                        event.setResult(Event.Result.DENY);
//                        return;
//                    } else if (!AttunementUtil.isItemAttunedToPlayer(player, stack) && !DataPackUtil.canUseWithoutAttunement(stack)) {
//                        PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.item_not_equippable");
//                        event.setResult(Event.Result.DENY);
//                        return;
//                    }
//                }
//            }
//        }
//        event.setResult(Event.Result.DEFAULT);
//    }
//      @SubscribeEvent
//      public void addCurioAttributeModifier(CurioAttributeModifierEvent curioAttributeModifierEvent) {
//          ResourceLocation attributeResourceLocation = new ResourceLocation(attribute);
//          Attribute attributeToModify = ForgeRegistries.ATTRIBUTES.getValue(attributeResourceLocation);
//          if(attributeToModify != null) {
//              curioAttributeModifierEvent.addModifier(attributeToModify, this.buildAttributeModifier());
//          }
//      }

//    @SubscribeEvent
//    public static void keepCurios(DropRulesEvent event) {
//        if (event.getEntity() instanceof Player player) {
//            if (!player.level().isClientSide()) {
//                changeSoulboundCuriosDropRule(player, event);
//            }
//        }
//    }
//
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
//
//    public static void changeSoulboundCuriosDropRule(Player player, DropRulesEvent event) {
//        CuriosApi.getCuriosInventory(player).ifPresent(itemHandler -> {
//            for (int i = 0; i < itemHandler.getSlots(); ++i) {
//                int finalI = i;
//                ItemStack curiosStack = itemHandler.getEquippedCurios().getStackInSlot(finalI);
//                if(AttunementUtil.isValidAttunementItem(curiosStack) && StackNBTUtil.isSoulbound(curiosStack)) {
//                    event.addOverride(stack -> stack == itemHandler.getEquippedCurios().getStackInSlot(finalI), ICurio.DropRule.ALWAYS_KEEP);
//                }
//            }
//        });
//    }
}
