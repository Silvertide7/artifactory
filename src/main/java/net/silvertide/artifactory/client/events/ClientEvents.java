package net.silvertide.artifactory.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.StackNBTUtil;

import java.util.List;

public class ClientEvents {
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if(!stack.isEmpty()) {
            DataPackUtil.getAttunementData(stack).ifPresent(itemAttunementData -> {
                event.getToolTip().add(createAttunementHoverComponent(itemAttunementData, stack));
                if(AttunementUtil.isAvailableToAttune(stack)){
                    addSlotTooltip(event.getToolTip(), itemAttunementData);
                }
            });
        }
    }

    private MutableComponent createAttunementHoverComponent(ItemAttunementData itemAttunementData, ItemStack stack) {
        if (AttunementUtil.isItemAttunedToAPlayer(stack)) {
            return createAttunedHoverText(stack);
        } else {
            return createUnattunedHoverText(itemAttunementData.useWithoutAttunement(), ChatFormatting.DARK_PURPLE);
        }
    }

    private MutableComponent createAttunedHoverText(ItemStack stack) {
        MutableComponent hoverText = Component.translatable("hovertext.artifactory.attuned_item").withStyle(ChatFormatting.DARK_PURPLE);
        StackNBTUtil.getAttunedToName(stack).ifPresent(name -> {
            hoverText.append(Component.literal(" (" + name + ")").withStyle(ChatFormatting.DARK_PURPLE));
        });
        return hoverText;
    }

    private MutableComponent createUnattunedHoverText(boolean useWithoutAttunement, ChatFormatting chatFormatting) {
        if(useWithoutAttunement){
            return Component.translatable("hovertext.artifactory.use_without_attunement").withStyle(chatFormatting);
        } else {
            return Component.translatable("hovertext.artifactory.use_with_attunement").withStyle(chatFormatting);
        }
    }

    private void addSlotTooltip(List<Component> toolTips, ItemAttunementData itemAttunementData) {
        if(itemAttunementData.getAttunementSlotsUsed() > 0) {
            String tooltipText = itemAttunementData.getAttunementSlotsUsed() + " slot";
            if(itemAttunementData.getAttunementSlotsUsed() > 1) tooltipText += "s";
            toolTips.add(Component.literal(tooltipText).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}

