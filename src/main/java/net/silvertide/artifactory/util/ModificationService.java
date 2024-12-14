package net.silvertide.artifactory.util;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.config.codecs.AttunementLevel;
import net.silvertide.artifactory.modifications.*;

import java.util.ArrayList;
import java.util.List;

public final class ModificationService {
    private ModificationService() {}

    // If datapack values were changed we want to check if any modification values have changed and update the item
    // to match. This can include applying new modifications or removing old modifications.
    public static void applyAttunementModifications(ItemStack stack) {
        applyAttunementModifications(stack, AttunementUtil.getLevelOfAttunementAchieved(stack));
    }

    public static void applyAttunementModifications(ItemStack stack, int levelOfAttunement) {
        if(levelOfAttunement > 0) {
            DataPackUtil.getAttunementData(stack).ifPresent(itemAttunementData -> {
                ModificationService.clearModifications(stack);

                List<AttunementLevel> attunementLevels = itemAttunementData.attunementLevels();

                int levelsToApply = Math.min(levelOfAttunement, attunementLevels.size());
                attunementLevels.subList(0, levelsToApply).stream()
                        .flatMap(attunementLevel -> attunementLevel.getModifications().stream())
                        .forEach(modification -> {
                            applyAttunementModification(stack, modification);
                        });
            });
        }
    }

    public static void applyAttunementModification(ItemStack stack, String modificationString) {
        ModificationFactory.createAttunementModification(modificationString).ifPresent(modification -> {
            modification.applyModification(stack);
        });
    }

    private static void clearModifications(ItemStack stack) {
        StackNBTUtil.removeUnbreakable(stack);
        StackNBTUtil.removeArtifactoryUnbreakable(stack);
        StackNBTUtil.removeInvulnerable(stack);
        StackNBTUtil.removeSoulbound(stack);
        StackNBTUtil.removeAttributeModifications(stack);
    }

}
