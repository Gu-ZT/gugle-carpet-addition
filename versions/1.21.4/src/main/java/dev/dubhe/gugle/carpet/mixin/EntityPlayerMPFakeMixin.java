package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.util.BotUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayerMPFake.class, remap = false)
public class EntityPlayerMPFakeMixin extends ServerPlayer {
    public EntityPlayerMPFakeMixin(
        MinecraftServer minecraftServer,
        ServerLevel serverLevel,
        GameProfile gameProfile,
        ClientInformation clientInformation
    ) {
        super(minecraftServer, serverLevel, gameProfile, clientInformation);
    }

    @Inject(method = "isSpawningPlayer", at = @At("RETURN"), cancellable = true)
    private static void isSpawningPlayer(String username, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && BotUtil.isGcaSpawningBot(username));
    }


    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void tickInject(CallbackInfo ci) {
        this.setScore(-114514);
    }
}
