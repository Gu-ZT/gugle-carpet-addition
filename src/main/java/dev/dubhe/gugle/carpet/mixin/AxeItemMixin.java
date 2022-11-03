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
public class AxeItemMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/AxeItem;getStripped(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"), cancellable = true)
    private void stripped(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = context.getItemInHand();
        String name = itemStack.getHoverName().getString();
        if (!name.contains("Strip") && !name.contains("去皮") && GcaSetting.betterWoodStrip) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
