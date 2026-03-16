//#if MC > 12001
package dev.dubhe.gugle.carpet.util;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GameProfileUtil {

    @Nullable
    public static GameProfile getGameProfile(MinecraftServer server, final String name) {
        GameProfileCache.setUsesAuthentication(false);
        GameProfile gameprofile = null;
        try {
            GameProfileCache cache = server.getProfileCache();
            if (cache != null) {
                gameprofile = cache.get(name).orElse(null);
            }
        } finally {
            GameProfileCache.setUsesAuthentication(server.isDedicatedServer() && server.usesAuthentication());
        }
        if (gameprofile == null && CarpetSettings.allowSpawningOfflinePlayers) {
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        }
        return gameprofile;
    }

    public static CompletableFuture<Optional<GameProfile>> fetchGameProfile(MinecraftServer server, GameProfile profile) {
        return SkullBlockEntity.fetchGameProfile(profile.getName());
    }

}
//#endif
