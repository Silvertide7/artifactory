package net.silvertide.artifactory.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.silvertide.artifactory.modifications.AttributeModification;
import net.silvertide.artifactory.util.*;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosCompat {

    // Check if the curios item should not be equipable and prevent it if so.
    @SubscribeEvent
    public static void onCuriosEquip(CurioCanEquipEvent event) {
        SlotContext slotContext = event.getSlotContext();

        if(!event.getEquipResult().isFalse() && slotContext.entity() instanceof Player) {
            if(slotContext.entity() instanceof Player player && !player.level().isClientSide()) {
                ItemStack stack = event.getStack();
                AttunementService.clearBrokenAttunementIfExists(stack);

                if(AttunementUtil.isValidAttunementItem(stack)) {
                    if(AttunementUtil.isAttunedToAnotherPlayer(player, stack)) {
                        PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.owned_by_another_player");
                        event.setEquipResult(TriState.FALSE);
                        return;
                    } else if (!AttunementUtil.isItemAttunedToPlayer(player, stack) && !DataPackUtil.canUseWithoutAttunement(stack)) {
                        PlayerMessenger.displayTranslatabelClientMessage(player,"playermessage.artifactory.item_not_equippable");
                        event.setEquipResult(TriState.FALSE);
                        return;
                    }
                }
            }
        }
        event.setEquipResult(TriState.DEFAULT);
    }

    @SubscribeEvent
    public void addCurioAttributeModifier(CurioAttributeModifierEvent curioAttributeModifierEvent) {
        ResourceLocation attributeResourceLocation = new ResourceLocation(attribute);
        Attribute attributeToModify = NeoForge.EVENT_BUS.getValue(attributeResourceLocation);
        if(attributeToModify != null) {
            curioAttributeModifierEvent.addModifier(attributeToModify, this.buildAttributeModifier());
        }
    }

    @SubscribeEvent
    public static void onCurioAttributeModifierEvent(CurioAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (AttunementUtil.isValidAttunementItem(stack) && StackNBTUtil.containsAttributeModifications(stack)) {
            CompoundTag artifactoryAttributeModificationsTag = StackNBTUtil.getOrCreateAttributeModificationNBT(stack);
            for(String attributeModificationKey : artifactoryAttributeModificationsTag.getAllKeys()) {
                AttributeModification.fromCompoundTag(artifactoryAttributeModificationsTag.getCompound(attributeModificationKey)).ifPresent(attributeModification -> {
                    attributeModification.addCurioAttributeModifier(event);
                });
            }
        }
    }

    @SubscribeEvent
    public static void keepCurios(DropRulesEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            changeSoulboundCuriosDropRule(serverPlayer, event);
        }
    }

    public static void changeSoulboundCuriosDropRule(ServerPlayer serverPlayer, DropRulesEvent event) {
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
