package net.silvertide.artifactory.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.state.ClientItemAttunementData;
import net.silvertide.artifactory.config.codecs.AttunementDataSource;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataComponentUtil;

import java.util.ArrayList;
import java.util.List;
@EventBusSubscriber(modid= Artifactory.MOD_ID, bus=EventBusSubscriber.Bus.GAME, value= Dist.CLIENT)
public class TooltipHandler {
    private static final ChatFormatting unAttunedFormatting = ChatFormatting.DARK_PURPLE;
    private static final ChatFormatting attunedFormatting = ChatFormatting.LIGHT_PURPLE;

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if(!stack.isEmpty() && ClientItemAttunementData.isValidAttunementItem(stack)) {
            ClientItemAttunementData.getAttunementData(stack).ifPresent(itemAttunementData -> {
                addTraitTooltips(event.getToolTip(), stack);
                createAttunementHoverComponent(event.getToolTip(), itemAttunementData, stack);
                addUniqueTooltip(event.getToolTip(), itemAttunementData);
            });
        }
    }

    private static void createAttunementHoverComponent(List<Component> toolTips, AttunementDataSource itemAttunementData, ItemStack stack) {
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

    private static MutableComponent createAttunedHoverText(ItemStack stack) {
        MutableComponent hoverText = Component.translatable("hovertext.artifactory.attuned_item").withStyle(attunedFormatting);
        DataComponentUtil.getAttunementData(stack).ifPresent(attunementData -> {
            if(attunementData.attunedToName() != null) {
                hoverText.append(Component.literal(" <" + attunementData.attunedToName() + ">").withStyle(attunedFormatting));
            }
        });
        return hoverText;
    }

    private static MutableComponent createUnattunedHoverText(boolean useWithoutAttunement) {
        if(useWithoutAttunement){
            return Component.translatable("hovertext.artifactory.use_without_attunement").withStyle(unAttunedFormatting);
        } else {
            return Component.translatable("hovertext.artifactory.use_with_attunement").withStyle(unAttunedFormatting);
        }
    }

    private static void addTraitTooltips(List<Component> toolTips, ItemStack stack) {
        List<String> traitTextList = DataComponentUtil.getAttunementData(stack).map(attunementData -> {
            List<String> textList = new ArrayList<>();
            if(attunementData.isSoulbound()) {
                textList.add("Soulbound");
            }

            if(attunementData.isInvulnerable()) {
                textList.add("Invulnerable");
            }

            return textList;
        }).orElse(new ArrayList<>());

        traitTextList.forEach(trait -> toolTips.add(Component.literal(trait).withStyle(ChatFormatting.DARK_BLUE)));
    }

    private static void addUniqueTooltip(List<Component> toolTips, AttunementDataSource itemAttunementData) {
        if(itemAttunementData.unique()) {
            toolTips.add(Component.translatable("hovertext.artifactory.unique").withStyle(ChatFormatting.GOLD));
        }
    }
}

