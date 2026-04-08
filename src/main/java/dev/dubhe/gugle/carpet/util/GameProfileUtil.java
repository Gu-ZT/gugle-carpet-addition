package dev.dubhe.gugle.carpet.util;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;

//#if MC > 12001
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
//#else
//$$ import java.util.concurrent.atomic.AtomicReference;
//#endif

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
        //#if MC <= 12001
        //$$ if (gameprofile != null && gameprofile.getProperties().containsKey("textures")) {
        //$$     AtomicReference<GameProfile> result = new AtomicReference<>();
        //$$     SkullBlockEntity.updateGameprofile(gameprofile, result::set);
        //$$     gameprofile = result.get();
        //$$ }
        //#endif
        return gameprofile;
    }

    //#if MC > 12001
    public static CompletableFuture<Optional<GameProfile>> fetchGameProfile(MinecraftServer server, GameProfile profile) {
        return SkullBlockEntity.fetchGameProfile(profile.getName());
    }
    //#endif
}
