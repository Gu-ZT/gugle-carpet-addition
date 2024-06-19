package dev.dubhe.gugle.carpet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dubhe.gugle.carpet.GcaSetting;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlaceRecipe.class)
abstract class ServerPlaceRecipeMixin {
    @WrapOperation(method = "handleRecipeClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/StackedContents;getBiggestCraftableStack(Lnet/minecraft/world/item/crafting/RecipeHolder;Lit/unimi/dsi/fastutil/ints/IntList;)I"))
    private int handleRecipeClicked(StackedContents instance, RecipeHolder<?> recipeHolder, IntList intList, Operation<Integer> original) {
        int i = original.call(instance, recipeHolder, intList);
        return GcaSetting.betterQuickCrafting ? i - 1 : i;
    }
}
