package net.silvertide.artifactory.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.client.keybindings.Keybindings;
import net.silvertide.artifactory.config.codecs.ItemAttunementData;
import net.silvertide.artifactory.network.PacketHandler;
import net.silvertide.artifactory.network.SB_ToggleManageAttunementsScreen;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataPackUtil;
import net.silvertide.artifactory.util.StackNBTUtil;

import java.util.List;

@Mod.EventBusSubscriber(modid = Artifactory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent clientTickEvent) {
        if(Minecraft.getInstance().player == null) return;
        if(Keybindings.INSTANCE.useOpenManageAttunementsKey.consumeClick()) {
            PacketHandler.sendToServer(new SB_ToggleManageAttunementsScreen());
        }
    }

    private final ChatFormatting unAttunedFormatting = ChatFormatting.DARK_PURPLE;
    private final ChatFormatting attunedFormatting = ChatFormatting.LIGHT_PURPLE;

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if(!stack.isEmpty() && AttunementUtil.isValidAttunementItem(stack)) {
            DataPackUtil.getAttunementData(stack).ifPresent(itemAttunementData -> {
                createAttunementHoverComponent(event.getToolTip(), itemAttunementData, stack);
                addTraitTooltips(event.getToolTip(), stack);
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
}