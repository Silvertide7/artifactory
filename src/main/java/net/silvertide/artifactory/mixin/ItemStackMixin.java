package net.silvertide.artifactory.mixin;

import net.minecraft.world.item.ItemStack;
import net.silvertide.artifactory.services.DurabilityService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @ModifyVariable(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int artifactory$reduceDurabilityLossForDurableItems(int durabilityLoss) {
        return DurabilityService.reduceDurabilityLossForDurableItems((ItemStack) (Object) this, durabilityLoss);
    }
}
