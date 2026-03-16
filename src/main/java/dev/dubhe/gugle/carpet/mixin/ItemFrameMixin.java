package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC>=12102
//$$ import net.minecraft.server.level.ServerLevel;
//#else
//#endif

@Mixin(ItemFrame.class)
abstract class ItemFrameMixin extends Entity {
    public ItemFrameMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method =
            //#if MC>=12102
            //$$ "dropItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Z)V",
            //#else
            "dropItem(Lnet/minecraft/world/entity/Entity;Z)V",
        //#endif
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/decoration/ItemFrame;setItem(Lnet/minecraft/world/item/ItemStack;)V"
            )
        },
        cancellable = true
    )
    private void dropItem(
        //#if MC>=12102
        //$$ ServerLevel serverLevel,
        //#endif
        Entity entity,
        boolean bl,
        CallbackInfo ci
    ) {
        if (GcaSetting.betterItemFrameInteraction) {
            if (entity instanceof Player player) {
                if (!player.getMainHandItem().is(Items.CACTUS) && !player.getOffhandItem().is(Items.CACTUS)) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(
        method = {"interact"},
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/decoration/ItemFrame;setRotation(I)V"
            )
        },
        cancellable = true
    )
    private void interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (GcaSetting.betterItemFrameInteraction) {
            if ((!player.getMainHandItem().is(Items.CACTUS) || !player.getOffhandItem().is(this::gca$isGlass)) && (
                !player.getOffhandItem()
                    .is(Items.CACTUS) || !player.getMainHandItem().is(this::gca$isGlass)
            )) {
                if (!player.getMainHandItem().is(Items.CACTUS) && !player.getOffhandItem().is(Items.CACTUS)) {
                    Level level = this.level();
                    Direction direction = this.getDirection();
                    BlockPos blockPos = this.getOnPos().relative(direction, -1);
                    BlockState blockState = level.getBlockState(blockPos);
                    BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(blockPos), direction, blockPos, false);
                    //#if MC>=12002
                    blockState.useWithoutItem(level, player, hitResult);
                    //#else
                    //$$ blockState.use(level, player, hand, hitResult);
                    //#endif
                    cir.setReturnValue(InteractionResult.CONSUME);
                }
            } else {
                this.setInvisible(!this.isInvisible());
                cir.setReturnValue(InteractionResult.CONSUME);
            }
        }
    }

    @Unique
    private boolean gca$isGlass(Holder<Item> itemHolder) {
        return itemHolder.is(BuiltInRegistries.ITEM.getKey(Items.GLASS)) || itemHolder.is(BuiltInRegistries.ITEM.getKey(Items.GLASS_PANE));
    }
}
