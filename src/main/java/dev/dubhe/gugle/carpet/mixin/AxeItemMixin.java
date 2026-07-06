package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
abstract class AxeItemMixin {
    @Inject(method = "useOn", at = @At(value = "HEAD"), cancellable = true)
    private void stripped(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!GcaSetting.betterWoodStrip) return;
        ItemStack stack = context.getItemInHand();
        String name = stack.getHoverName().getString();
        if (name.toLowerCase().contains("strip") || name.contains("去皮")) {
            return;
        }
        cir.setReturnValue(InteractionResult.PASS);
    }
}
