package net.silvertide.artifactory.modifications;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.NBTUtil;

public class BasicModification implements AttunementModification {
    BasicModificationType modificationType;

    public BasicModification(BasicModificationType modificationType){
        this.modificationType = modificationType;
    }

    @Override
    public void applyModification(ItemStack stack) {
        switch(modificationType) {
            case UNBREAKABLE -> applyUnbreakable(stack);
            case FIREPROOF -> applyFireproof(stack);
            case SOULBOUND -> applySoulbound(stack);
        }
    }

    private void applySoulbound(ItemStack stack) {
        Artifactory.LOGGER.info("Applying soulbound - need to implement.");
    }

    private void applyFireproof(ItemStack stack) {
        Artifactory.LOGGER.info("Applying fireproof - need to implement.");
    }

    private void applyUnbreakable(ItemStack stack) {
        if(stack.isDamageableItem()){
            stack.setDamageValue(0);
            NBTUtil.setBoolean(stack, "Unbreakable", true);
        }
    }
}
