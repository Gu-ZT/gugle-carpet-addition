package dev.dubhe.gugle.carpet.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.dubhe.gugle.carpet.config.ConfigUpdater;
import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainServerMixin {
    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;spin(Ljava/util/function/Function;)Lnet/minecraft/server/MinecraftServer;"))
    private static void updateConfig(String[] strings, CallbackInfo ci, @Local LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        ConfigUpdater.tryUpdateOldVersion(levelStorageAccess);
    }
}
