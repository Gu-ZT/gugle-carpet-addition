package dev.dubhe.gugle.carpet.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"
            , at = @At("HEAD"), cancellable = true)
    private void drop(ItemStack droppedItem, boolean dropAround, boolean includeThrowerName,
            CallbackInfoReturnable<ItemEntity> cir) {
        if (droppedItem.getTag() != null) {
            if (droppedItem.getTag().get("GcaClear") != null) {
                if (droppedItem.getTag().getBoolean("GcaClear")) {
                    droppedItem.setCount(0);
                    cir.setReturnValue(null);
                }
            }
        }
    }
}
