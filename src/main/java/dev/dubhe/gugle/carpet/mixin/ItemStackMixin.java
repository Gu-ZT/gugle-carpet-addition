package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    ItemStack self = (ItemStack) (Object) this;

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void tick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
        if (!(entity instanceof EntityPlayerMPFake)) {
            if (self.getTag() != null) {
                if (self.getTag().get("GcaClear") != null) {
                    if (self.getTag().getBoolean("GcaClear")) {
                        self.setCount(0);
                    }
                }
            }
        }
    }
}
