package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplaceTool;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#if MC >= 12104
//$$ import dev.dubhe.gugle.carpet.util.BotUtil;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif
//#if MC > 12001
import net.minecraft.server.level.ClientInformation;
//#endif

@Mixin(value = EntityPlayerMPFake.class, remap = false)
public abstract class EntityPlayerMPFakeMixin extends ServerPlayer {
    public EntityPlayerMPFakeMixin(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile
        //#if MC > 12001
        , ClientInformation clientInformation
        //#endif
    ) {
        super(minecraftServer, serverLevel, gameProfile
            //#if MC > 12001
            , clientInformation
            //#endif
        );
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void doTick(CallbackInfo ci) {
        if (!"false".equals(GcaSetting.fakePlayerAutoReplaceTool)) {
            FakePlayerAutoReplaceTool.tryReplaceTool(this);
        }
    }

    //#if MC >= 12104
    //$$ @Inject(method = "isSpawningPlayer", at = @At("RETURN"), cancellable = true)
    //$$ private static void isSpawningPlayer(String username, CallbackInfoReturnable<Boolean> cir) {
    //$$     cir.setReturnValue(cir.getReturnValue() && BotUtil.isGcaSpawningBot(username));
    //$$ }
    //#endif
}
