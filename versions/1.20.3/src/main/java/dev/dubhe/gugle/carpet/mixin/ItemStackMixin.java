package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplaceTool;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerNotification;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

    @WrapOperation(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"))
    private boolean onHurt(ItemStack itemStack, int i, RandomSource randomSource, ServerPlayer player, Operation<Boolean> original) {
        boolean broken = original.call(itemStack, i, randomSource, player);
        // 物品耐久耗尽，交给下方的Mixin方法处理
        if (broken || !(player instanceof EntityPlayerMPFake fakePlayer)) return broken;
        if (!"false".equals(GcaSetting.fakePlayerAutoReplaceTool)) {
            FakePlayerAutoReplaceTool.checkFakePlayerShouldReplaceTool(fakePlayer, itemStack.getItem(), itemStack);
        }
        return false;
    }

    @WrapOperation(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private <T extends LivingEntity> void onShrink(ItemStack itemStack, int i, Operation<Void> original, @Local(argsOnly = true) T livingEntity) {
        ItemStack beforeItem = itemStack.copy();
        original.call(itemStack, i);
        if (livingEntity instanceof EntityPlayerMPFake fakePlayer) {
            if (!"false".equals(GcaSetting.fakePlayerAutoReplaceTool)) {
                FakePlayerAutoReplaceTool.checkFakePlayerShouldReplaceTool(fakePlayer, beforeItem.getItem(), itemStack);
            } else if (GcaSetting.fakePlayerToolDamagedNotification && itemStack.isEmpty()) { // 如果开了工具替换, 替换后再确认是否通知
                FakePlayerNotification.sendToolDamaged(fakePlayer, beforeItem);
            }
        }
    }
}

