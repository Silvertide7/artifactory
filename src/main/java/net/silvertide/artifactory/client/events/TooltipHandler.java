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
import net.silvertide.artifactory.util.StackNBTUtil;

import java.util.ArrayList;
import java.util.List;
public class TooltipHandler {
    private final ChatFormatting unAttunedFormatting = ChatFormatting.DARK_PURPLE;
    private final ChatFormatting attunedFormatting = ChatFormatting.LIGHT_PURPLE;

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if(!stack.isEmpty() && ClientItemAttunementData.isValidAttunementItem(stack)) {
            ClientItemAttunementData.getAttunementData(stack).ifPresent(itemAttunementData -> {
                addTraitTooltips(event.getToolTip(), stack);
                createAttunementHoverComponent(event.getToolTip(), itemAttunementData, stack);
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
        List<String> textList = new ArrayList<>();
        if(StackNBTUtil.isInvulnerable(stack)) {
            textList.add("Invulnerable");
        }

        if(StackNBTUtil.isSoulbound(stack)) {
            textList.add("Soulbound");
        }
        textList.forEach(trait -> toolTips.add(Component.literal(trait).withStyle(ChatFormatting.BLUE)));
    }

    private void addUniqueTooltip(List<Component> toolTips, ItemAttunementData itemAttunementData) {
        if(itemAttunementData.unique()) {
            toolTips.add(Component.translatable("hovertext.artifactory.unique").withStyle(ChatFormatting.GOLD));
        }
    }
}

