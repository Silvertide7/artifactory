package net.silvertide.artifactory.modifications;

import net.minecraft.core.component.DataComponents;
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
            case SOULBOUND -> applySoulbound(stack);
            case RESILIENT -> applyResilient(stack);
        }
    }

    private void applySoulbound(ItemStack stack) {
        DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
            if(!attunementData.isSoulbound()) {
                DataComponentUtil.setPlayerAttunementData(stack, attunementData.withIsSoulbound(true));
            }
        });
    }

    private void applyResilient(ItemStack stack) {
        if(stack.has(DataComponents.MAX_DAMAGE)) {
            DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
                if(!attunementData.isResilient()) {
                    DataComponentUtil.setPlayerAttunementData(stack, attunementData.withIsResilient(true));
                }
            });
        }
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
            case SOULBOUND -> "Soulbound";
            case RESILIENT -> "Resilient";
        };
    }
}
