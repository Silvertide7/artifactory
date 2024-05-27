package net.silvertide.artifactory.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.util.ArtifactUtil;
import net.silvertide.artifactory.util.AttunementDataUtil;
import net.silvertide.artifactory.util.StackNBTUtil;

public class ClientEvents {
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent evt) {
        ItemStack stack = evt.getItemStack();
        if(!stack.isEmpty()) {
            AttunementDataUtil.getAttunementData(stack).ifPresent(itemAttunementData -> {
                evt.getToolTip().add(1, createAttunementHoverComponent(itemAttunementData, stack));
            });
        }
    }

    private MutableComponent createAttunementHoverComponent(ItemAttunementData itemAttunementData, ItemStack stack) {
        MutableComponent hoverText;
        ChatFormatting chatFormatting;

        if (ArtifactUtil.isItemAttuned(stack)) {
            chatFormatting = ChatFormatting.LIGHT_PURPLE;
            hoverText = createAttunedHoverText(stack, chatFormatting);
        } else {
            chatFormatting = ChatFormatting.DARK_PURPLE;
            hoverText = createUnattunedHoverText(itemAttunementData.useWithoutAttunement(), chatFormatting);
        }
        hoverText.append(Component.literal(" (" + itemAttunementData.getAttunementSlotsUsed() + ")").withStyle(chatFormatting));
        return hoverText;
    }

    private MutableComponent createAttunedHoverText(ItemStack stack, ChatFormatting chatFormatting){
        MutableComponent hoverText = Component.translatable("hovertext.artifactory.attuned_item").withStyle(chatFormatting);
        StackNBTUtil.getAttunedToName(stack).ifPresent(name -> {
            hoverText.append(Component.literal(" (" + name + ")").withStyle(chatFormatting));
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
}

