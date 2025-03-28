package net.silvertide.artifactory.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.TriState;
import net.silvertide.artifactory.client.state.ClientItemAttunementData;
import net.silvertide.artifactory.services.AttunementService;
import net.silvertide.artifactory.services.PlayerMessenger;
import net.silvertide.artifactory.util.*;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosEvents {
    // Check if the curios item should not be equipable and prevent it if so.
    public static void onCuriosEquip(CurioCanEquipEvent event) {
        SlotContext slotContext = event.getSlotContext();

        if(slotContext.entity() instanceof ServerPlayer serverPlayer) {
            ItemStack stack = event.getStack();
            AttunementService.checkAndUpdateAttunementComponents(stack);

            if(AttunementUtil.isValidAttunementItem(stack)) {
                //If a player tries to equip an item attuned to another player to ANY slot, deny it.
                if(AttunementUtil.isAttunedToAnotherPlayer(serverPlayer, stack)) {
                    PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.owned_by_another_player");
                    event.setEquipResult(TriState.FALSE);
                }
                // If a player tries to equip an item they are not attuned to and that item must be attuned to use to ANY slot, deny it.
                else if (!AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack) && !AttunementDataSourceUtil.canUseWithoutAttunement(stack)) {
                    PlayerMessenger.displayTranslatabelClientMessage(serverPlayer,"playermessage.artifactory.item_not_equippable");
                    event.setEquipResult(TriState.FALSE);
                }
            }
        }
    }

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

    public static void onCurioAttributeModifierEvent(CurioAttributeModifierEvent event) {
        // Don't apply attributes if placed into an attuned_item slot
        if(!"attuned_item".equals(event.getSlotContext().identifier())) {
            // Check the artifactory attributes data and apply attribute modifiers
            ItemStack stack = event.getItemStack();
            boolean isValidAttunementItem = switch(FMLEnvironment.dist) {
                case CLIENT -> ClientItemAttunementData.isValidAttunementItem(stack);
                case DEDICATED_SERVER -> AttunementUtil.isValidAttunementItem(stack);
            };

            if(isValidAttunementItem) {
                DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
                    attunementData.attributeModifications().forEach(modification -> modification.addAttributeModifier(event));
                });
            }
        }
    }
}
