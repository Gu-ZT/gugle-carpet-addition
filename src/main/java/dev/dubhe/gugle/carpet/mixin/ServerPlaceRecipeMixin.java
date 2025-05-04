package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;

import net.minecraft.recipebook.ServerPlaceRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//#if MC>=12102
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#elseif MC>=12100
import net.minecraft.world.item.crafting.RecipeHolder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.player.StackedContents;
import org.jetbrains.annotations.NotNull;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//#else
//$$ import net.minecraft.world.item.crafting.Recipe;
//$$ import it.unimi.dsi.fastutil.ints.IntList;
//$$ import net.minecraft.world.entity.player.StackedContents;
//$$ import org.jetbrains.annotations.NotNull;
//$$ import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//$$ import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//#endif

@Mixin(ServerPlaceRecipe.class)
abstract class ServerPlaceRecipeMixin {
    //#if MC>=12102
    //$$ @Inject(method = "calculateAmountToCraft", at = @At("RETURN"), cancellable = true)
    //$$ private void calculateAmountToCraft(int i, boolean bl, CallbackInfoReturnable<Integer> cir) {
    //$$     if (GcaSetting.betterQuickCrafting) cir.setReturnValue(cir.getReturnValueI() - 1);
    //$$ }
    //#elseif MC>=12100
    @WrapOperation(method = "handleRecipeClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/StackedContents;getBiggestCraftableStack(Lnet/minecraft/world/item/crafting/RecipeHolder;Lit/unimi/dsi/fastutil/ints/IntList;)I"))
    private int handleRecipeClicked(StackedContents instance, RecipeHolder<?> recipeHolder, IntList intList, @NotNull Operation<Integer> original) {
        int i = original.call(instance, recipeHolder, intList);
        return GcaSetting.betterQuickCrafting ? i - 1 : i;
    }
    //#else
    //$$ @WrapOperation(method = "handleRecipeClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/StackedContents;getBiggestCraftableStack(Lnet/minecraft/world/item/crafting/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;)I"))
    //$$ private int handleRecipeClicked(StackedContents instance, Recipe<?> recipe, IntList intList, Operation<Integer> original) {
    //$$     int i = original.call(instance, recipe, intList);
    //$$     return GcaSetting.betterQuickCrafting ? i - 1 : i;
    //$$ }
    //#endif
}
