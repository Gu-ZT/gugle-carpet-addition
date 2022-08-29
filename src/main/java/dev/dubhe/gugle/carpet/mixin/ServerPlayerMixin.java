package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    ServerPlayer self = (ServerPlayer) (Object) this;

    @Inject(method = "initInventoryMenu", at = @At("RETURN"))
    private void load(CallbackInfo ci) {
        if (GcaSetting.fakePlayerResident && self.getServer() != null && self.getServer().isSingleplayerOwner(self.getGameProfile())) {
            GcaExtension.onServerStart(self.getServer());
        }
    }
}
