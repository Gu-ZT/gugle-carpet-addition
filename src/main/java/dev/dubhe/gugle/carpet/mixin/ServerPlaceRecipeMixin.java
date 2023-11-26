package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin {
    @Redirect(method = "handleRecipeClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/StackedContents;getBiggestCraftableStack(Lnet/minecraft/world/item/crafting/RecipeHolder;Lit/unimi/dsi/fastutil/ints/IntList;)I"))
    private <C extends Container> int handleRecipeClicked(StackedContents instance, RecipeHolder<? extends Recipe<C>> recipeHolder, IntList integers) {
        int i = instance.getBiggestCraftableStack(recipeHolder, integers);
        return GcaSetting.betterQuickCrafting ? i - 1 : i;
    }
}
