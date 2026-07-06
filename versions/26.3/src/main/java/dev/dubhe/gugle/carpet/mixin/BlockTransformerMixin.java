package dev.dubhe.gugle.carpet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockTransformer.class)
public class BlockTransformerMixin {

    @WrapOperation(method = "transformBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/BlockTransformer;playerHasBlockingItemUseIntent(Lnet/minecraft/world/item/context/UseOnContext;)Z"))
    private boolean passAxeUse(UseOnContext context, Operation<Boolean> original) {
        if (original.call(context)) return true;
        ItemStack stack = context.getItemInHand();
        if (GcaSetting.betterWoodStrip && stack.is(ItemTags.AXES)) {
            String name = stack.getHoverName().getString();
            return !name.toLowerCase().contains("strip") && !name.contains("去皮");
        }
        return false;
    }

}
