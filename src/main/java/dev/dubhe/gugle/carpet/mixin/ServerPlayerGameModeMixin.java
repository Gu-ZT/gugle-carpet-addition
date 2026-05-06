package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplenishment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Inject(method = "useItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult" +
        //#if MC < 12102
        "Holder" +
        //#endif
        ";"))
    private void useItem(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (GcaSetting.fakePlayerAutoReplenishment && serverPlayer instanceof EntityPlayerMPFake) {
            FakePlayerAutoReplenishment.autoReplenishment(serverPlayer, hand);
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER))
    private void useItemOn(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (GcaSetting.fakePlayerAutoReplenishment && serverPlayer instanceof EntityPlayerMPFake) {
            FakePlayerAutoReplenishment.autoReplenishment(serverPlayer, hand);
        }

    }
}
