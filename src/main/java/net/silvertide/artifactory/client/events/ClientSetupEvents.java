package net.silvertide.artifactory.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silvertide.artifactory.client.state.ClientItemAttunementData;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.StackNBTUtil;

import java.util.List;
public class ClientSetupEvents {
    private final ChatFormatting unAttunedFormatting = ChatFormatting.DARK_PURPLE;
    private final ChatFormatting attunedFormatting = ChatFormatting.LIGHT_PURPLE;

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if(!stack.isEmpty() && ClientItemAttunementData.isValidAttunementItem(stack)) {
            ClientItemAttunementData.getAttunementData(stack).ifPresent(itemAttunementData -> {
                createAttunementHoverComponent(event.getToolTip(), itemAttunementData, stack);
                addTraitTooltips(event.getToolTip(), stack);
                addUniqueTooltip(event.getToolTip(), itemAttunementData);
            });
        }
    }

    private void createAttunementHoverComponent(List<Component> toolTips, ItemAttunementData itemAttunementData, ItemStack stack) {
        MutableComponent toolTip;
        if (AttunementUtil.isItemAttunedToAPlayer(stack)) {
            toolTip = createAttunedHoverText(stack);
        } else {
            toolTip = createUnattunedHoverText(itemAttunementData.useWithoutAttunement());
            if(itemAttunementData.getAttunementSlotsUsed() > 0) {
                String tooltipText = " - " + itemAttunementData.getAttunementSlotsUsed() + " slot";
                if(itemAttunementData.getAttunementSlotsUsed() > 1) tooltipText += "s";
                toolTip.append(Component.literal(tooltipText).withStyle(unAttunedFormatting));
            }
        }
        toolTips.add(toolTip);
    }

    private MutableComponent createAttunedHoverText(ItemStack stack) {
        MutableComponent hoverText = Component.translatable("hovertext.artifactory.attuned_item").withStyle(attunedFormatting);
        StackNBTUtil.getAttunedToName(stack).ifPresent(name -> {
            hoverText.append(Component.literal(" <" + name + ">").withStyle(attunedFormatting));
        });
        return hoverText;
    }

    private MutableComponent createUnattunedHoverText(boolean useWithoutAttunement) {
        if(useWithoutAttunement){
            return Component.translatable("hovertext.artifactory.use_without_attunement").withStyle(unAttunedFormatting);
        } else {
            return Component.translatable("hovertext.artifactory.use_with_attunement").withStyle(unAttunedFormatting);
        }
    }

    private void addTraitTooltips(List<Component> toolTips, ItemStack stack) {
        String traitText = null;
        if(StackNBTUtil.isSoulbound(stack)) {
            traitText = "Soulbound";
        }

        if(StackNBTUtil.isInvulnerable(stack)) {
            if(traitText != null){
                traitText += " | Invulnerable";
            } else {
                traitText = "Invulnerable";
            }
        }
        if(traitText != null) {
            toolTips.add(Component.literal(traitText).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    private void addUniqueTooltip(List<Component> toolTips, ItemAttunementData itemAttunementData) {
        if(itemAttunementData.unique()) {
            toolTips.add(Component.translatable("hovertext.artifactory.unique").withStyle(ChatFormatting.GOLD));
        }
    }
}

