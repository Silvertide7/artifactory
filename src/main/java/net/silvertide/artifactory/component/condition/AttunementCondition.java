package net.silvertide.artifactory.component.condition;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface AttunementCondition {
    String type();
    boolean isMetBy(ItemStack stack);
    Component failureDescription();
}
