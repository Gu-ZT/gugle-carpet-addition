package dev.dubhe.gugle.carpet.mixin;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplaceTool;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

//#if MC < 12109
import java.util.Optional;
//#else
//$$ import java.util.UUID;
//#endif
//#if MC >= 12104
//$$ import dev.dubhe.gugle.carpet.util.BotUtil;
//#endif
//#if MC > 12001
import net.minecraft.server.level.ClientInformation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.CompletableFuture;
//#else
//$$ import com.mojang.authlib.properties.PropertyMap;
//#endif

@Mixin(EntityPlayerMPFake.class)
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

    @Inject(method = "die", at = @At("HEAD"))
    private void onDie(DamageSource cause, CallbackInfo ci) {
        if ("false".equals(GcaSetting.fakePlayerAutoRespawn)) return;
        EntityPlayerActionPack pack = ((ServerPlayerInterface) this).getActionPack();
        FakePlayerAutoRespawn.onFakePlayerDied(this.uuid, pack);
    }

    //#if MC >= 12104
    //$$ @Inject(method = "isSpawningPlayer", at = @At("RETURN"), cancellable = true, remap = false)
    //$$ private static void isSpawningPlayer(String username, CallbackInfoReturnable<Boolean> cir) {
    //$$     cir.setReturnValue(cir.getReturnValue() && BotUtil.isGcaSpawningBot(username));
    //$$ }
    //#endif

    //#if MC < 12109
    @Nullable
    @WrapOperation(method = "createFake", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElse(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <T> T useOfflineUUID(Optional<T> instance, T other, Operation<T> original) {
        if (GcaSetting.fakePlayerForceOfflineUUID) return null;
        return original.call(instance, other);
    }
    //#else
    //$$ @Nullable
    //$$ @WrapOperation(method = "createFake", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/OldUsersConverter;convertMobOwnerIfNecessary(Lnet/minecraft/server/MinecraftServer;Ljava/lang/String;)Ljava/util/UUID;"))
    //$$ private static UUID useOfflineUUID(MinecraftServer minecraftServer, String string, Operation<UUID> original) {
    //$$     if (GcaSetting.fakePlayerForceOfflineUUID) return null;
    //$$     return original.call(minecraftServer, string);
    //$$ }
    //#endif


    //#if MC > 12001
    @Inject(method = "fetchGameProfile", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cancelFetchUUID(
        //#if MC >= 12109
        //$$ MinecraftServer server, UUID name, CallbackInfoReturnable<CompletableFuture<GameProfile>> cir
        //#else
        String name, CallbackInfoReturnable<CompletableFuture<Optional<GameProfile>>> cir
        //#endif
    ) {
        if (GcaSetting.fakePlayerForceOfflineUUID) {
            cir.setReturnValue(CompletableFuture.completedFuture(
                //#if MC >= 12109
                //$$ new GameProfile(name, "")
                //#else
                Optional.empty()
                //#endif
            ));
        }
    }
    //#else
    //$$ @WrapOperation(method = "createFake", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/properties/PropertyMap;containsKey(Ljava/lang/Object;)Z"), remap = false)
    //$$ private static boolean cancelFetchUUID(PropertyMap instance, Object o, Operation<Boolean> original) {
    //$$     if (GcaSetting.fakePlayerForceOfflineUUID) {
    //$$         return false;
    //$$     }
    //$$     return original.call(instance, o);
    //$$ }
    //#endif
}
