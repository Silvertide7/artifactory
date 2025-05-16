package net.silvertide.artifactory.modifications;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.util.DataComponentUtil;

public class BasicModification implements AttunementModification {
    final BasicModificationType modificationType;

    public BasicModification(BasicModificationType modificationType){
        this.modificationType = modificationType;
    }

    @Override
    public void applyModification(ItemStack stack) {
        switch(modificationType) {
            case UNBREAKABLE -> applyUnbreakable(stack);
            case INVULNERABLE -> applyInvulnerable(stack);
            case SOULBOUND -> applySoulbound(stack);
        }
    }

    private void applySoulbound(ItemStack stack) {
        DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
            if(!attunementData.isSoulbound()) {
                DataComponentUtil.setPlayerAttunementData(stack, attunementData.withIsSoulbound(true));
            }
        });
    }

    private void applyInvulnerable(ItemStack stack) {
        DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
            if(!attunementData.isInvulnerable()) {
                DataComponentUtil.setPlayerAttunementData(stack, attunementData.withIsInvulnerable(true));
            }
        });
    }

    private void applyUnbreakable(ItemStack stack) {
        if(stack.isDamageableItem()) {
            DataComponentUtil.makeUnbreakable(stack);
        }
    }

    @Override
    public String toString() {
        return switch(modificationType) {
            case UNBREAKABLE -> "Unbreakable";
            case INVULNERABLE -> "Invulnerable";
            case SOULBOUND -> "Soulbound";
        };
    }
}
