package net.silvertide.artifactory.mixin;

import net.minecraft.world.entity.Entity;
import net.silvertide.artifactory.services.VoidReturnService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "onBelowWorld", at = @At("HEAD"), cancellable = true)
    private void artifactory$returnAttunedItemBeforeVoidDeletesIt(CallbackInfo ci) {
        if (VoidReturnService.returnAttunedItemToOwner((Entity) (Object) this)) ci.cancel();
    }
}
