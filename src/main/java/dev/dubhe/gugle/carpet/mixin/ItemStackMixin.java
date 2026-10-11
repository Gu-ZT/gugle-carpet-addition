package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplaceTool;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerNotification;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 12100
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import java.util.function.Consumer;
//#else
//$$ import net.minecraft.util.RandomSource;
//#endif

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Shadow
    @Final
    //#if MC >= 260000
    //$$ private
    //#endif
    PatchedDataComponentMap components;

    @WrapOperation(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At(value = "INVOKE", target =
        //#if MC >= 12100
        "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"
        //#else
        //$$ "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/Runnable;)V"
        //#endif
    ))
    private void hurtAndBreak(
        ItemStack itemStack, int i,
        //#if MC >= 12100
        ServerLevel
            //#else
            //$$ RandomSource
            //#endif
            source, ServerPlayer serverPlayer,
        //#if MC >= 12100
        Consumer<Item>
            //#else
            //$$ Runnable
            //#endif
            runnable, Operation<Void> original, @Local(argsOnly = true) EquipmentSlot equipmentSlot
    ) {
        // 在物品损坏前获取物品类型，损坏后将只能获取为空气
        ItemStack beforeItem = itemStack.copy();
        original.call(itemStack, i, source, serverPlayer, runnable);
        if (!(serverPlayer instanceof EntityPlayerMPFake fakePlayer)) return;
        if (!"false".equals(GcaSetting.fakePlayerAutoReplaceTool)) {
            FakePlayerAutoReplaceTool.checkFakePlayerShouldReplaceTool(fakePlayer, beforeItem.getItem(), equipmentSlot);
        } else if (GcaSetting.fakePlayerToolDamagedNotification && itemStack.isEmpty()) { // 如果开了工具替换, 替换后再确认是否通知
            FakePlayerNotification.sendToolDamaged(fakePlayer, beforeItem);
        }
    }

    @Inject(method = "getComponents", at = @At("HEAD"), cancellable = true)
    private void getComponents(CallbackInfoReturnable<DataComponentMap> cir) {
        CustomData customData = this.components.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.copyTag().get(Button.GCA_CLEAR) == null) {
            return;
        }
        cir.setReturnValue(this.components);
    }
}

