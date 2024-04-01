package net.silvertide.artifactory.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.util.ArtifactUtil;
import net.silvertide.artifactory.util.StackNBTUtil;

public class ClientEvents {

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent evt) {
        ItemStack stack = evt.getItemStack();
        if(!stack.isEmpty()) {
            ArtifactUtil.getAttunementData(stack).ifPresent(itemAttunementData -> {
                evt.getToolTip().add(1, createAttunementHoverComponent(itemAttunementData, stack));
            });
        }
    }

    private MutableComponent createAttunementHoverComponent(ItemAttunementData itemAttunementData, ItemStack stack) {
        MutableComponent hoverText;

        if (ArtifactUtil.isItemAttuned(stack)) {
            hoverText = Component.translatable("hovertext.artifactory.attunedItem").withStyle(ChatFormatting.LIGHT_PURPLE);
            StackNBTUtil.getAttunedToName(stack).ifPresent(name -> {
                hoverText.append(Component.literal(" to " + name).withStyle(ChatFormatting.LIGHT_PURPLE));
            });
        } else {
            hoverText = Component.translatable("hovertext.artifactory.attuneableItem").withStyle(ChatFormatting.DARK_PURPLE);
            if (!itemAttunementData.useWithoutAttunement()){
                hoverText.append(Component.translatable("hovertext.artifactory.onlyUseIfAttuned").withStyle(ChatFormatting.RED));
            }
        }
        hoverText.append(Component.literal(" (" + itemAttunementData.getAttunementSlotsUsed() + ")").withStyle(ChatFormatting.YELLOW));
        return hoverText;
    }
}

