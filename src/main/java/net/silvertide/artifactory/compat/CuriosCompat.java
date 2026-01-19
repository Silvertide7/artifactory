package net.silvertide.artifactory.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.silvertide.artifactory.modifications.AttributeModification;
import net.silvertide.artifactory.util.*;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosCompat {

    public static void initialize(IEventBus eventBus) {
        CompatFlags.CURIOS_LOADED = true;

        eventBus.addListener(CuriosCompat::onCuriosEquip);
        eventBus.addListener(CuriosCompat::keepCurios);
        eventBus.addListener(CuriosCompat::onCurioAttributeModifierEvent);

        CuriosApi.registerCurioPredicate(new ResourceLocation("artifactory", "is_attuned_item"), (SlotResult slotResult) -> {
            if(slotResult.slotContext().entity() instanceof ServerPlayer serverPlayer) {
                ItemStack stack = slotResult.stack();
                if(AttunementUtil.isValidAttunementItem(stack)) {
                    if(!AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack)) {
                        PlayerMessenger.displayTranslatableClientMessage(serverPlayer,"playermessage.artifactory.item_not_equippable");
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void onCuriosEquip(CurioEquipEvent event) {
        SlotContext ctx = event.getSlotContext();

        if (!(ctx.entity() instanceof ServerPlayer serverPlayer)) return;

        ItemStack stack = event.getStack();
        AttunementService.clearBrokenAttunementIfExists(stack);

        if (!AttunementUtil.isValidAttunementItem(stack)) return;

        if (AttunementUtil.isAttunedToAnotherPlayer(serverPlayer, stack)) {
            PlayerMessenger.displayTranslatableClientMessage(serverPlayer,
                    "playermessage.artifactory.owned_by_another_player");
            event.setResult(Event.Result.DENY);
            return;
        }

        if (!AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack)
                && !DataPackUtil.canUseWithoutAttunement(stack)) {
            PlayerMessenger.displayTranslatableClientMessage(serverPlayer,
                    "playermessage.artifactory.item_not_equippable");
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void keepCurios(DropRulesEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            changeSoulboundCuriosDropRule(player, event);
        }
    }

    public static void changeSoulboundCuriosDropRule(ServerPlayer player, DropRulesEvent event) {
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

    @SubscribeEvent
    public static void onCurioAttributeModifierEvent(CurioAttributeModifierEvent event) {
        SlotContext ctx = event.getSlotContext();
        if (!(ctx.entity() instanceof ServerPlayer)) return;

        // Don't apply attributes if placed into an attuned_item slot
        if(!"attuned_item".equals(ctx.identifier())) {
            // Check the artifactory attributes data and apply attribute modifiers
            ItemStack stack = event.getItemStack();
            boolean isValidAttunementItem = AttunementUtil.isValidAttunementItem(stack);

            if(isValidAttunementItem && StackNBTUtil.containsAttributeModifications(stack)) {
                CompoundTag artifactoryAttributeModificationsTag = StackNBTUtil.getOrCreateAttributeModificationNBT(stack);
                for(String attributeModificationKey : artifactoryAttributeModificationsTag.getAllKeys()) {
                    AttributeModification.fromCompoundTag(artifactoryAttributeModificationsTag.getCompound(attributeModificationKey)).ifPresent(attributeModification -> {
                        addCurioAttributeModifier(event, attributeModification);
                    });
                }
            }
        }
    }

    public static void addCurioAttributeModifier(CurioAttributeModifierEvent curioAttributeModifierEvent, AttributeModification modification) {
        ResourceLocation attributeResourceLocation = new ResourceLocation(modification.getAttribute());
        Attribute attributeToModify = ForgeRegistries.ATTRIBUTES.getValue(attributeResourceLocation);
        if(attributeToModify != null) {
            curioAttributeModifierEvent.addModifier(attributeToModify, modification.buildAttributeModifier());
        }
    }

    public static void ejectInvalidCurios(ServerPlayer serverPlayer) {
        CuriosApi.getCuriosInventory(serverPlayer).ifPresent(inv -> {
            inv.getCurios().forEach((identifier, handler) -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStacks().getStackInSlot(i);
                    if (stack.isEmpty()) continue;

                    AttunementService.clearBrokenAttunementIfExists(stack);
                    if (AttunementUtil.isValidAttunementItem(stack)
                            && !AttunementUtil.isItemAttunedToPlayer(serverPlayer, stack)
                            && !DataPackUtil.canUseWithoutAttunement(stack)) {

                        ItemStack removed = handler.getStacks().extractItem(i, stack.getCount(), false);

                        if (!serverPlayer.getInventory().add(removed)) {
                            serverPlayer.drop(removed, false);
                        }
                    }
                }
            });
        });
    }
}
