package net.silvertide.artifactory.compat;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.silvertide.artifactory.services.AttunementService;
import net.silvertide.artifactory.util.DataComponentUtil;
import net.silvertide.quality_api.api.Qualities;
import net.silvertide.quality_api.api.QualityBreakDowngradeEvent;

public final class QualityCompat {
    private QualityCompat() {}

    public static void initialize(IEventBus gameEventBus) {
        CompatFlags.QUALITY_LOADED = true;
        gameEventBus.addListener(QualityCompat::keepQualityOfAttunedItems);
    }

    public static boolean hasQualityAtLeast(ItemStack stack, int minQuality) {
        return Qualities.getId(stack).map(Qualities::levelOf).filter(level -> level >= minQuality).isPresent();
    }

    private static void keepQualityOfAttunedItems(QualityBreakDowngradeEvent event) {
        ItemStack stack = event.getStack();
        if(!DataComponentUtil.isActivelyAttuned(stack)) return;
        AttunementService.checkAndUpdateAttunementComponents(stack);
        if(DataComponentUtil.isActivelyAttuned(stack)) event.setCanceled(true);
    }
}
