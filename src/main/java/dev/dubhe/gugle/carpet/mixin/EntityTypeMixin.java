package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 260200
//$$ import net.minecraft.world.entity.EntityTypes;
//#endif

@Mixin(EntityType.class)
abstract class EntityTypeMixin {
    @Inject(method = "updateInterval", at = @At("HEAD"), cancellable = true)
    private void updateInterval(CallbackInfoReturnable<Integer> cir) {
        if (!GcaSetting.fixedEndCrystalSync) return;
        if ((Object) this !=
            //#if MC < 260200
            EntityType.END_CRYSTAL
            //#else
            //$$ EntityTypes.END_CRYSTAL
            //#endif
        ) return;
        cir.setReturnValue(20);
    }
}
