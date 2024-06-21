package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
abstract class AxeItemMixin {
    @Inject(method = "useOn", at = @At(value = "HEAD"), cancellable = true)
    private void stripped(@NotNull UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = context.getItemInHand();
        String name = itemStack.getHoverName().getString();
        if (GcaSetting.betterWoodStrip) {
            if (name.contains("Strip") || name.contains("去皮")) {
                return;
            }
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
