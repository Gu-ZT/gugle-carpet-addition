package dev.dubhe.gugle.carpet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import dev.dubhe.gugle.carpet.GcaSetting;

import net.minecraft.recipebook.ServerPlaceRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlaceRecipe.class)
abstract class ServerPlaceRecipeMixin {

    @Shadow
    @Final
    private boolean useMaxItems;

    @WrapOperation(method = "placeRecipe(Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/world/entity/player/StackedItemContents;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipebook/ServerPlaceRecipe;calculateAmountToCraft(IZ)I"))
    private int calculateAmountToCraft(ServerPlaceRecipe<?> instance, int i, boolean bl, Operation<Integer> original, @Cancellable CallbackInfo cir) {
        if (GcaSetting.betterQuickCrafting && this.useMaxItems) {
            if (i <= 1) {
                cir.cancel();
                return 0;
            }
            i--;
        }
        return original.call(instance, i, bl);
    }

}
