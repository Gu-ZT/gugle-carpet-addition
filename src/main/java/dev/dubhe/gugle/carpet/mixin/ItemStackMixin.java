package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplaceTool;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplenishment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
//#if MC>=12104
//$$ import net.minecraft.world.InteractionResult;
//#else
import net.minecraft.world.InteractionResultHolder;
//#endif
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import com.llamalad7.mixinextras.sugar.Local;
//#if MC>=12100
import java.util.function.Consumer;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.component.CustomData;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
//#else
//$$ import net.minecraft.util.RandomSource;
//$$ import net.minecraft.world.entity.LivingEntity;
//#endif

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    //#if MC>=12100
    @Shadow
    public abstract Item getItem();

    @Shadow
    @Final
    PatchedDataComponentMap components;
    //#endif


    @Inject(method = "use", at = @At("HEAD"))
    private void use(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<
        //#if MC>=12104
        //$$ InteractionResult
        //#else
        InteractionResultHolder<ItemStack>
        //#endif
        > cir) {
        if (GcaSetting.fakePlayerAutoReplenishment && player instanceof EntityPlayerMPFake fakePlayer) {
            FakePlayerAutoReplenishment.autoReplenishment(fakePlayer);
        }
    }

    //#if MC>=12100
    @WrapOperation(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"))
    private void hurtAndBreak(ItemStack itemStack, int i, ServerLevel serverLevel, ServerPlayer serverPlayer, Consumer<Item> consumer, Operation<Void> original, @Local(argsOnly = true) EquipmentSlot equipmentSlot) {
        // 在物品损坏前获取物品类型，损坏后将只能获取为空气
        Item item = itemStack.getItem();
        original.call(itemStack, i, serverLevel, serverPlayer, consumer);
        if (GcaSetting.fakePlayerAutoReplaceTool && serverPlayer instanceof EntityPlayerMPFake fakePlayer) {
            FakePlayerAutoReplaceTool.autoReplaceTool(fakePlayer, item, equipmentSlot);
        }
    }
    //#else
    //$$ @WrapOperation(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"))
    //$$ private boolean onHurt(ItemStack itemStack, int i, RandomSource randomSource, ServerPlayer player, Operation<Boolean> original) {
    //$$     Boolean call = original.call(itemStack, i, randomSource, player);
    //$$     if (call) {
    //$$         // 物品耐久耗尽，交给下方的Mixin方法处理
    //$$         return true;
    //$$     }
    //$$     if (GcaSetting.fakePlayerAutoReplaceTool && player instanceof EntityPlayerMPFake fakePlayer) {
    //$$         FakePlayerAutoReplaceTool.autoReplaceTool(fakePlayer, itemStack.getItem(), itemStack);
    //$$     }
    //$$     return false;
    //$$ }
    //$$
    //$$ @WrapOperation(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    //$$ private <T extends LivingEntity> void onShrink(ItemStack itemStack, int i, Operation<Void> original, @Local(argsOnly = true) T livingEntity) {
    //$$     Item item = itemStack.getItem();
    //$$     original.call(itemStack, i);
    //$$     if (GcaSetting.fakePlayerAutoReplaceTool && livingEntity instanceof EntityPlayerMPFake fakePlayer) {
    //$$         FakePlayerAutoReplaceTool.autoReplaceTool(fakePlayer, item, itemStack);
    //$$     }
    //$$ }
    //#endif


    //#if MC>=12100
    @Inject(method = "getComponents", at = @At("HEAD"), cancellable = true)
    private void getComponents(CallbackInfoReturnable<DataComponentMap> cir) {
        CustomData customData = this.components.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.copyTag().get(Button.GCA_CLEAR) == null) {
            return;
        }
        cir.setReturnValue(this.components);
    }
    //#endif
}

