package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @SuppressWarnings("ConstantValue")
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interact(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        // 在客户端中，玩家可以与客户端的被交互玩家交互并返回PASS，这时交互玩家手上如果拿着可以使用的物品，则物品会被使用
        // 所以如果判断被交互实体是客户端玩家，返回SUCCESS
        if ((Object) this instanceof RemotePlayer && (GcaSetting.openFakePlayerInventory || GcaSetting.openFakePlayerEnderChest)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
