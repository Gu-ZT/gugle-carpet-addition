package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMPFake.class)
abstract class EntityPlayerMPFakeMixin extends ServerPlayer {

    public EntityPlayerMPFakeMixin(
        MinecraftServer minecraftServer,
        ServerLevel serverLevel,
        GameProfile gameProfile,
        ClientInformation clientInformation
    ) {
        super(minecraftServer, serverLevel, gameProfile, clientInformation);
    }

    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void tickInject(CallbackInfo ci) {
        this.setScore(-114514);
    }
}
