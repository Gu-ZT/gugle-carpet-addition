package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplaceTool;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplenishment;
import net.minecraft.world.InteractionHand;
//#if MC>=12104
//$$ import net.minecraft.world.InteractionResult;
//#else
import net.minecraft.world.InteractionResultHolder;
//#endif
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC>=12100
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
//#else
//$$ import java.util.function.Consumer;
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
    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"))
    private void hurtAndBreak(int i, LivingEntity entity, EquipmentSlot equipmentSlot, CallbackInfo ci) {
        //#else
        //$$ @Inject(method = "hurtAndBreak", at = @At("HEAD"))
        //$$ private <T extends LivingEntity> void hurtAndBreak(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        //#endif
        if (GcaSetting.fakePlayerAutoReplaceTool && entity instanceof EntityPlayerMPFake fakePlayer) {
            FakePlayerAutoReplaceTool.autoReplaceTool(fakePlayer);
        }
    }

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

