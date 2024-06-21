package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.FakePlayerAutoReplaceTool;
import dev.dubhe.gugle.carpet.tools.FakePlayerAutoReplenishment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void use(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (GcaSetting.fakePlayerAutoReplenishment && player instanceof EntityPlayerMPFake fakePlayer) {
            FakePlayerAutoReplenishment.autoReplenishment(fakePlayer);
        }
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"))
    private void hurtAndBreak(int i, LivingEntity livingEntity, EquipmentSlot equipmentSlot, CallbackInfo ci) {
        if (GcaSetting.fakePlayerAutoReplaceTool && livingEntity instanceof EntityPlayerMPFake fakePlayer) {
            FakePlayerAutoReplaceTool.autoReplaceTool(fakePlayer);
        }
    }
}

