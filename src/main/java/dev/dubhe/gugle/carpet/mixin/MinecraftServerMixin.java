package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Environment(EnvType.SERVER)
    @Inject(method = "loadLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V"))
    private void loadLevel(CallbackInfo ci) {
        GcaExtension.onServerStart((MinecraftServer) (Object) this);
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;stopServer()V"))
    private void serverStop(CallbackInfo ci) {
        GcaExtension.onServerStop((MinecraftServer) (Object) this);
    }
}
