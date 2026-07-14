package dev.dubhe.gugle.carpet.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerRespawnLogic.class)
public interface PlayerRespawnLogicInvoker {
    @Invoker
    static BlockPos invokeGetOverworldRespawnPos(ServerLevel serverLevel, int i, int j) {
        throw new AssertionError();
    }
}
