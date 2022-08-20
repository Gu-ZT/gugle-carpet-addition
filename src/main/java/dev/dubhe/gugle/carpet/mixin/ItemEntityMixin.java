package dev.dubhe.gugle.carpet.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    ItemEntity self = (ItemEntity) (Object) this;

    @Inject(method = "tick",at=@At("HEAD"))
    private void tick(CallbackInfo ci){
        ItemStack selfItem = self.getItem();
        if (selfItem.getTag() != null) {
            if (selfItem.getTag().get("GcaClear") != null) {
                if (selfItem.getTag().getBoolean("GcaClear")) {
                    self.kill();
                }
            }
        }
    }
}
