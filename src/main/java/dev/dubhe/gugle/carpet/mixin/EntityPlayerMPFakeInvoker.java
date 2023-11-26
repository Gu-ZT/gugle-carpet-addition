package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(EntityPlayerMPFake.class)
public interface EntityPlayerMPFakeInvoker {
    @Invoker(value = "fetchGameProfile", remap = false)
    static CompletableFuture<Optional<GameProfile>> fetchGameProfile(String name) {
        throw new AssertionError();
    }

    @Invoker(value = "<init>", remap = false)
    static EntityPlayerMPFake create(MinecraftServer server, ServerLevel worldIn, GameProfile profile, ClientInformation cli, boolean shadow) {
        throw new AssertionError();
    }
}
