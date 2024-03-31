package net.silvertide.artifactory.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.silvertide.artifactory.util.ArtifactUtil;
import net.silvertide.artifactory.util.NBTUtil;

import java.util.ArrayList;
import java.util.List;

public class ClientEvents {

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent evt) {
        ItemStack stack = evt.getItemStack();
        if(!stack.isEmpty()) {
            ArtifactUtil.getAttunementData(stack).ifPresent(itemAttunementData -> {
                List<Component> artifactory_tooltips = new ArrayList<>();
                if (ArtifactUtil.isItemAttuned(stack)) {
                    NBTUtil.getAttunedToName(stack).ifPresent(name -> artifactory_tooltips.add(Component.literal("Attuned to " + name).withStyle(ChatFormatting.LIGHT_PURPLE)));
                } else {
                    artifactory_tooltips.add(Component.literal("Attuneable (" + itemAttunementData.attunementSlotsUsed() + ")").withStyle(ChatFormatting.DARK_PURPLE));
                }
                evt.getToolTip().addAll(1, artifactory_tooltips);
            });
        }
    }
}

