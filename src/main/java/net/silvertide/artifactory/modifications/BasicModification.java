package net.silvertide.artifactory.modifications;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.util.StackNBTUtil;

public class BasicModification implements AttunementModification {
    BasicModificationType modificationType;

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
        StackNBTUtil.setSoulbound(stack);
    }

    private void applyInvulnerable(ItemStack stack) {
        StackNBTUtil.setInvulnerable(stack);
    }

    private void applyUnbreakable(ItemStack stack) {
        if(stack.isDamageableItem()){
            StackNBTUtil.setUnbreakable(stack);
        }
    }

    public static void removeUnbreakable(ItemStack stack) {

    }
}
