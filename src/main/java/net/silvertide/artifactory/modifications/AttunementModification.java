package net.silvertide.artifactory.modifications;

import net.minecraft.world.item.ItemStack;

public interface AttunementModification {
    void applyModification(ItemStack stack);
}
