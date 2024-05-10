package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin {
    @Unique
    ServerPlayer gca$self = (ServerPlayer) (Object) this;

    @Inject(method = "initInventoryMenu", at = @At("RETURN"))
    private void load(CallbackInfo ci) {
        if (GcaSetting.fakePlayerResident && gca$self.getServer() != null && gca$self.getServer().isSingleplayerOwner(gca$self.getGameProfile())) {
            GcaExtension.onServerStart(gca$self.getServer());
        }
    }
}
