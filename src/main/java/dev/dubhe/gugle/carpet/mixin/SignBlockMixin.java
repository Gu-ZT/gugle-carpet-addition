package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlock.class)
public abstract class SignBlockMixin {
    private final SignBlock self = (SignBlock) (Object) this;

    @Inject(method = "use", at = @At(value = "INVOKE",target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;)V"), cancellable = true)
    public void use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (GcaSetting.betterSignInteraction && self instanceof WallSignBlock) {
            Direction direction = state.getValue(WallSignBlock.FACING);
            BlockPos blockPos = pos.relative(direction, -1);
            BlockState blockState = level.getBlockState(blockPos);
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(blockPos), direction, blockPos, false);
            if (blockState.getBlock() instanceof WallSignBlock) return;
            else blockState.use(level, player, hand, hitResult);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
