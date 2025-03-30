package net.silvertide.artifactory.services;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.AttunementSchema;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.component.AttunementLevel;
import net.silvertide.artifactory.modifications.*;
import net.silvertide.artifactory.util.AttunementSchemaUtil;
import net.silvertide.artifactory.util.AttunementUtil;
import net.silvertide.artifactory.util.DataComponentUtil;

import java.util.ArrayList;
import java.util.List;

public final class ModificationService {
    private ModificationService() {}

    // If datapack values were changed we want to check if any modification values have changed and update the item
    // to match. This can include applying new modifications or removing old modifications.
    public static void applyAttunementModifications(ItemStack stack) {
        applyAttunementModifications(stack, AttunementUtil.getLevelOfAttunementAchieved(stack));
    }

    //TODO Might want to refactor this to look at sources only, and also use the data component to check if it has player attunement data, thats equivalent to having a level > 0
    public static void applyAttunementModifications(ItemStack stack, int levelOfAttunement) {
        if(levelOfAttunement > 0) {
            AttunementSchemaUtil.getAttunementSchema(stack).filter(AttunementSchema::isValidSchema).ifPresent(attunementSchema -> {
                ModificationService.clearModifications(stack);

                List<AttunementLevel> attunementLevels = attunementSchema.attunementLevels();

                int levelsToApply = Math.min(levelOfAttunement, attunementLevels.size());
                if(levelsToApply > 0) {
                    attunementLevels.subList(0, levelsToApply).stream()
                            .flatMap(attunementLevel -> attunementLevel.modifications().stream())
                            .forEach(modification -> {
                                applyAttunementModification(stack, modification);
                            });
                }
            });
        }
    }

    private static void applyAttunementModification(ItemStack stack, String modificationString) {
        ModificationFactory.createAttunementModification(modificationString).ifPresent(modification -> {
            modification.applyModification(stack);
        });
    }

    private static void clearModifications(ItemStack stack) {
        DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData -> {
            PlayerAttunementData clearedPlayerAttunementData = attunementData
                    .withIsInvulnerable(false)
                    .withIsSoulbound(false)
                    .withAttributeModifications(new ArrayList<>());

            if(clearedPlayerAttunementData.isUnbreakable() && DataComponentUtil.isUnbreakable(stack)) {
                DataComponentUtil.removeUnbreakable(stack);
            }
            DataComponentUtil.setPlayerAttunementData(stack, clearedPlayerAttunementData.withIsUnbreakable(false));
        });
    }
}
