package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(EntityPlayerMPFake.class)
public interface EntityPlayerMPFakeInvoker {
    @Invoker(value = "fetchGameProfile", remap = false)
    static CompletableFuture<Optional<GameProfile>> invokerFetchGameProfile(String name) {
        throw new AssertionError();
    }
}
