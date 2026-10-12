package net.silvertide.artifactory.modifications;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.Artifactory;
import net.silvertide.artifactory.util.DataComponentUtil;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class DurableModification implements AttunementModification {
    private static final Pattern DECIMAL_NUMBER = Pattern.compile("\\d+(\\.\\d+)?");
    private static final int DISPLAYED_DECIMAL_PLACES = 2;
    private final float durabilityMultiplier;

    private DurableModification(float durabilityMultiplier) {
        this.durabilityMultiplier = durabilityMultiplier;
    }

    public static DurableModification fromAttunementDataString(String durableModificationDataString) {
        String[] parts = durableModificationDataString.split("/");
        if (parts.length != 2 || !DECIMAL_NUMBER.matcher(parts[1]).matches()) {
            Artifactory.LOGGER.warn("Artifactory - Invalid durable modification {}, expected durable/<number of at least 1>", durableModificationDataString);
            return null;
        }
        float durabilityMultiplier = Float.parseFloat(parts[1]);
        if (!Float.isFinite(durabilityMultiplier) || durabilityMultiplier < 1.0F) {
            Artifactory.LOGGER.warn("Artifactory - Invalid durable modification {}, the multiplier must be at least 1", durableModificationDataString);
            return null;
        }
        return new DurableModification(durabilityMultiplier);
    }

    @Override
    public void applyModification(ItemStack stack) {
        if (!stack.has(DataComponents.MAX_DAMAGE)) return;
        DataComponentUtil.getPlayerAttunementData(stack).ifPresent(attunementData ->
                DataComponentUtil.setPlayerAttunementData(stack, attunementData.withDurabilityMultiplier(Math.min(attunementData.durabilityMultiplier() * durabilityMultiplier, Float.MAX_VALUE))));
    }

    public static String formatMultiplier(float durabilityMultiplier) {
        return BigDecimal.valueOf(durabilityMultiplier).setScale(DISPLAYED_DECIMAL_PLACES, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    @Override
    public @NotNull String toString() {
        return "Durable ×" + formatMultiplier(durabilityMultiplier);
    }
}
