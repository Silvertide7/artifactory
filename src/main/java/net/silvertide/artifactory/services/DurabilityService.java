package net.silvertide.artifactory.services;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.component.PlayerAttunementData;
import net.silvertide.artifactory.util.DataComponentUtil;

public final class DurabilityService {
    private static final RandomSource DURABILITY_LOSS_RANDOM = RandomSource.createThreadSafe();

    private DurabilityService() {}

    public static boolean isDurabilityMultiplierActive(float durabilityMultiplier) {
        return Float.isFinite(durabilityMultiplier) && durabilityMultiplier > 1.0F;
    }

    public static int reduceDurabilityLossForDurableItems(ItemStack stack, int durabilityLoss) {
        if (durabilityLoss <= 0 || !stack.isDamageableItem() || !DataComponentUtil.isActivelyAttuned(stack)) return durabilityLoss;
        float durabilityMultiplier = DataComponentUtil.getPlayerAttunementData(stack).map(PlayerAttunementData::durabilityMultiplier).orElse(1.0F);
        if (!isDurabilityMultiplierActive(durabilityMultiplier)) return durabilityLoss;

        double chanceToTakeEachPoint = 1.0D / durabilityMultiplier;
        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
        long pointsConsidered = 0;
        int durabilityLossTaken = 0;
        while (true) {
            pointsConsidered += pointsUntilNextOneIsTaken(chanceToTakeEachPoint);
            if (pointsConsidered > durabilityLoss) return durabilityLossTaken;
            durabilityLossTaken++;
            if (durabilityLossTaken >= remainingDurability) return durabilityLoss;
        }
    }

    private static long pointsUntilNextOneIsTaken(double chanceToTakeEachPoint) {
        double uniformInOpenUnitInterval = 1.0D - DURABILITY_LOSS_RANDOM.nextDouble();
        return 1L + (long) Math.floor(Math.log(uniformInOpenUnitInterval) / Math.log1p(-chanceToTakeEachPoint));
    }
}
