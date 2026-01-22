package net.silvertide.artifactory.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.TriState;
import net.silvertide.artifactory.modifications.AttributeModification;
import net.silvertide.artifactory.services.AttunementService;
import net.silvertide.artifactory.services.PlayerMessenger;
import net.silvertide.artifactory.util.*;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosCompat {

    private static boolean initialized = false;

    public static void initialize(IEventBus eventBus) {
        if(initialized) return;
        initialized = true;

        CompatFlags.CURIOS_LOADED = true;
        eventBus.addListener(CuriosCompat::onCuriosEquip);
        eventBus.addListener(CuriosCompat::keepCurios);
        eventBus.addListener(CuriosCompat::onCurioAttributeModifierEvent);

        CuriosApi.registerCurioPredicate(ResourceLocation.fromNamespaceAndPath("artifactory", "is_attuned_item"), (SlotResult slotResult) -> {
            if(slotResult.slotContext().entity() instanceof ServerPlayer serverPlayer) {
                ItemStack stack = slotResult.stack();
                if(AttunementUtil.isValidAttunementItem(stack)) {
                    return AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack);
                }
            }
            return false;
        });
    }

    // Check if the curios item should not be equipable and prevent it if so.
    public static void onCuriosEquip(CurioCanEquipEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        ItemStack stack = event.getStack();
        AttunementService.checkAndUpdateAttunementComponents(stack);

        if(AttunementUtil.isValidAttunementItem(stack)) {
            //If a player tries to equip an item attuned to another player to ANY slot, deny it.
            if(AttunementUtil.isAttunedToAnotherPlayer(serverPlayer, stack)) {
                PlayerMessenger.displayTranslatableClientMessage(serverPlayer,"playermessage.artifactory.owned_by_another_player");
                event.setEquipResult(TriState.FALSE);
            }
            // If a player tries to equip an item they are not attuned to and that item must be attuned to use to ANY slot, deny it.
            else if (!AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack) && !AttunementSchemaUtil.canUseWithoutAttunement(stack)) {
                PlayerMessenger.displayTranslatableClientMessage(serverPlayer,"playermessage.artifactory.item_not_equippable");
                event.setEquipResult(TriState.FALSE);
            }
        }
    }

    public static void keepCurios(DropRulesEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        CuriosApi.getCuriosInventory(serverPlayer).ifPresent(itemHandler -> {
            for (int i = 0; i < itemHandler.getSlots(); ++i) {
                ItemStack curiosStack = itemHandler.getEquippedCurios().getStackInSlot(i);
                if(AttunementUtil.isSoulboundActive(serverPlayer, curiosStack)) {
                    event.addOverride(stack -> stack == curiosStack, ICurio.DropRule.ALWAYS_KEEP);
                }
            }
        });
    }

    public static void onCurioAttributeModifierEvent(CurioAttributeModifierEvent event) {
        SlotContext slotContext = event.getSlotContext();
        if(!(slotContext.entity() instanceof ServerPlayer)) return;
        if("attuned_item".equals(slotContext.identifier())) return;

        // Don't apply attributes if placed into an attuned_item slot
        // Check the artifactory attributes data and apply attribute modifiers
        ItemStack stack = event.getItemStack();
        if(AttunementUtil.isValidAttunementItem(stack)) {
            DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
                attunementData.attributeModifications().forEach(modification -> addAttributeModifier(event, modification));
            });
        }
    }

    public static void addAttributeModifier(CurioAttributeModifierEvent curioAttributeModifierEvent, AttributeModification attributeModification) {
        curioAttributeModifierEvent.addModifier(attributeModification.attribute(), attributeModification.modifier());
    }

    public static void ejectInvalidCurios(ServerPlayer serverPlayer) {
        CuriosApi.getCuriosInventory(serverPlayer).ifPresent(inv -> {
            inv.getCurios().forEach((identifier, handler) -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStacks().getStackInSlot(i);
                    if (stack.isEmpty()) continue;

                    AttunementService.checkAndUpdateAttunementComponents(stack);

                    if (AttunementUtil.isValidAttunementItem(stack)
                            && !AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack)
                            && !AttunementSchemaUtil.canUseWithoutAttunement(stack)) {

                        ItemStack removed = handler.getStacks().extractItem(i, stack.getCount(), false);
                        if (removed.isEmpty()) continue;

                        if (!serverPlayer.getInventory().add(removed)) {
                            serverPlayer.drop(removed, false);
                        }
                    }
                }
            });
        });
    }
}
