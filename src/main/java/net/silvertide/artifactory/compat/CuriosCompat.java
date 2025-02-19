package net.silvertide.artifactory.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.silvertide.artifactory.client.state.ClientItemAttunementData;
import net.silvertide.artifactory.modifications.AttributeModification;
import net.silvertide.artifactory.util.*;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosCompat {

    @SubscribeEvent
    public static void onCuriosEquip(CurioEquipEvent event) {
        SlotContext slotContext = event.getSlotContext();

        if(!event.isCanceled() && slotContext.entity() instanceof Player) {
            if(slotContext.entity() instanceof ServerPlayer serverPlayer) {
                ItemStack stack = event.getStack();
                AttunementService.clearBrokenAttunementIfExists(stack);

                if(AttunementUtil.isValidAttunementItem(stack)) {
                    //If a player tries to equip an item attuned to another player to ANY slot, deny it.
                    if(AttunementUtil.isAttunedToAnotherPlayer(serverPlayer, stack)) {
                        PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.owned_by_another_player");
                        event.setResult(Event.Result.DENY);
                    }
                    // If a player tries to equip an item they are not attuned to and that item must be attuned to use to ANY slot, deny it.
                    else if (!AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack) && !DataPackUtil.canUseWithoutAttunement(stack)) {
                        PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.item_not_equippable");
                        event.setResult(Event.Result.DENY);
                    }
                }
            }
        }
        event.setResult(Event.Result.DEFAULT);
    }

    @SubscribeEvent
    public static void keepCurios(DropRulesEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!player.level().isClientSide()) {
                changeSoulboundCuriosDropRule(player, event);
            }
        }
    }

    @SubscribeEvent
    public static void onCurioAttributeModifierEvent(CurioAttributeModifierEvent event) {
        // Don't apply attributes if placed into an attuned_item slot
        if(!"attuned_item".equals(event.getSlotContext().identifier())) {
            // Check the artifactory attributes data and apply attribute modifiers
            ItemStack stack = event.getItemStack();
            boolean isValidAttunementItem = switch(FMLEnvironment.dist) {
                case CLIENT -> ClientItemAttunementData.isValidAttunementItem(stack);
                case DEDICATED_SERVER -> AttunementUtil.isValidAttunementItem(stack);
            };

            if(isValidAttunementItem && StackNBTUtil.containsAttributeModifications(stack)) {
                CompoundTag artifactoryAttributeModificationsTag = StackNBTUtil.getOrCreateAttributeModificationNBT(stack);
                for(String attributeModificationKey : artifactoryAttributeModificationsTag.getAllKeys()) {
                    AttributeModification.fromCompoundTag(artifactoryAttributeModificationsTag.getCompound(attributeModificationKey)).ifPresent(attributeModification -> {
                        attributeModification.addCurioAttributeModifier(event);
                    });
                }
            }
        }
    }

    public static void changeSoulboundCuriosDropRule(Player player, DropRulesEvent event) {
        CuriosApi.getCuriosInventory(player).ifPresent(itemHandler -> {
            for (int i = 0; i < itemHandler.getSlots(); ++i) {
                int finalI = i;
                ItemStack curiosStack = itemHandler.getEquippedCurios().getStackInSlot(finalI);
                if(AttunementUtil.isValidAttunementItem(curiosStack) && StackNBTUtil.isSoulbound(curiosStack)) {
                    event.addOverride(stack -> stack == itemHandler.getEquippedCurios().getStackInSlot(finalI), ICurio.DropRule.ALWAYS_KEEP);
                }
            }
        });
    }
}
