package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

@Mixin(FenceGateBlock.class)
public abstract class FenceGateBlockMixin {
    @Shadow
    @Final
    public static BooleanProperty OPEN;
    @Shadow
    @Final
    public static BooleanProperty POWERED;
    @Shadow
    @Final
    public static BooleanProperty IN_WALL;
    FenceGateBlock self = (FenceGateBlock) (Object) this;

    @Inject(method = "getStateForPlacement", at = @At(value = "RETURN"), cancellable = true)
    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        Level level = context.getLevel();
        BlockPos blockPos = context.hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (GcaSetting.betterFenceGatePlacement && level.getBlockState(blockPos).getBlock() instanceof FenceGateBlock) {
            boolean bl = level.hasNeighborSignal(blockPos) || blockState.getValue(OPEN);
            boolean bl1 = level.hasNeighborSignal(blockPos);
            Direction direction = blockState.getValue(FACING);
            Direction.Axis axis = direction.getAxis();
            boolean bl2 = axis == Direction.Axis.Z && (this.isWall(level.getBlockState(blockPos.west())) || this.isWall(level.getBlockState(blockPos.east()))) || axis == Direction.Axis.X && (this.isWall(level.getBlockState(blockPos.north())) || this.isWall(level.getBlockState(blockPos.south())));
            cir.setReturnValue(self.defaultBlockState().setValue(FACING, direction).setValue(OPEN, bl).setValue(POWERED, bl1).setValue(IN_WALL, bl2));
        }
    }

    @Shadow
    protected abstract boolean isWall(BlockState state);
}
