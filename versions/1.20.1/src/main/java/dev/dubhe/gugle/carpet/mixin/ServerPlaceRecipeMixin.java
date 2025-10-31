package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.recipebook.ServerPlaceRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.item.crafting.Recipe;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.player.StackedContents;

@Mixin(ServerPlaceRecipe.class)
abstract class ServerPlaceRecipeMixin {
    @WrapOperation(method = "handleRecipeClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/StackedContents;getBiggestCraftableStack(Lnet/minecraft/world/item/crafting/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;)I"))
    private int handleRecipeClicked(StackedContents instance, Recipe<?> recipe, IntList intList, Operation<Integer> original) {
        int i = original.call(instance, recipe, intList);
        return GcaSetting.betterQuickCrafting ? i - 1 : i;
    }
}
