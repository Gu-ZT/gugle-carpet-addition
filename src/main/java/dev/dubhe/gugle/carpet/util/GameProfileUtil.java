package dev.dubhe.gugle.carpet.util;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;

import javax.annotation.Nullable;

//#if MC > 12001
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
//#else
//$$ import java.util.concurrent.atomic.AtomicReference;
//#endif
//#if MC >= 12002 && MC <= 12004
//$$ import dev.dubhe.gugle.carpet.mixin.SkullBlockEntityAccessor;
//#else
import net.minecraft.world.level.block.entity.SkullBlockEntity;
//#endif

public class GameProfileUtil {

    @Nullable
    public static GameProfile getGameProfile(MinecraftServer server, final String name) {
        GameProfile gameprofile = null;
        if (!GcaSetting.fakePlayerForceOfflineUUID) {
            GameProfileCache.setUsesAuthentication(false);
            try {
                GameProfileCache cache = server.getProfileCache();
                if (cache != null) {
                    gameprofile = cache.get(name).orElse(null);
                }
            } finally {
                GameProfileCache.setUsesAuthentication(server.isDedicatedServer() && server.usesAuthentication());
            }
        }
        if (gameprofile == null && CarpetSettings.allowSpawningOfflinePlayers) {
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        }
        //#if MC <= 12001
        //$$ if (gameprofile != null && !GcaSetting.fakePlayerForceOfflineUUID && gameprofile.getProperties().containsKey("textures")) {
        //$$     AtomicReference<GameProfile> result = new AtomicReference<>();
        //$$     SkullBlockEntity.updateGameprofile(gameprofile, result::set);
        //$$     gameprofile = result.get();
        //$$ }
        //#endif
        return gameprofile;
    }

    //#if MC > 12001
    public static CompletableFuture<Optional<GameProfile>> fetchGameProfile(MinecraftServer server, GameProfile profile) {
        if (GcaSetting.fakePlayerForceOfflineUUID) {
            return CompletableFuture.completedFuture(Optional.of(profile));
        }
        //#if MC >= 12002 && MC <= 12004
        //$$ return SkullBlockEntityAccessor.invokeFetchGameProfile(profile.getName());
        //#else
        return SkullBlockEntity.fetchGameProfile(profile.getName());
        //#endif
    }
    //#endif
}
