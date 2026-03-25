package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.config.updater.ConfigUpdater;
import net.minecraft.client.Minecraft;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 12109
//$$ import org.spongepowered.asm.mixin.Final;
//$$ import org.spongepowered.asm.mixin.Shadow;
//#else
import com.llamalad7.mixinextras.sugar.Local;
//#endif
//#if MC >= 260000
//$$ import java.util.Optional;
//$$ import net.minecraft.world.level.gamerules.GameRules;
//#endif

@Mixin(Minecraft.class)
public class MinecraftMixin {

    //#if MC >= 12109
    //$$ @Shadow
    //$$ @Final
    //$$ private Services services;
    //#endif

    @Inject(method = "doWorldLoad", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;spin(Ljava/util/function/Function;)Lnet/minecraft/server/MinecraftServer;"))
    private void updateConfig(
        //#if MC <= 12002
        //$$ String string,
        //#endif
        LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem,
        //#if MC >= 260000
        //$$ Optional<GameRules> gameRules,
        //#endif
        boolean bl, CallbackInfo ci
        //#if MC < 12109
        , @Local Services services
        //#endif
        ) {
        ConfigUpdater.tryUpdateOldVersion(levelStorageAccess, services, false);
    }

}
