package dev.dubhe.gugle.carpet.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    ItemEntity self = (ItemEntity) (Object) this;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        ItemStack selfItem = self.getEntityData().get(ItemEntity.DATA_ITEM);
        if (selfItem.getTag() != null) {
            if (selfItem.getTag().get("GcaClear") != null) {
                if (selfItem.getTag().getBoolean("GcaClear")) {
                    self.kill();
                }
            }
        }
    }

    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
    private void getItem(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack selfItem = self.getEntityData().get(ItemEntity.DATA_ITEM);
        if (selfItem.getTag() != null) {
            if (selfItem.getTag().get("GcaClear") != null) {
                if (selfItem.getTag().getBoolean("GcaClear")) {
                    cir.setReturnValue(ItemStack.EMPTY);
                }
            }
        }
    }
}
