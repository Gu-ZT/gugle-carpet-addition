package dev.dubhe.gugle.carpet.util;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GameProfileUtil {

    public static GameProfile getGameProfile(MinecraftServer server, final String name) {
        UUID uuid = null;
        if (!GcaSetting.fakePlayerForceOfflineUUID) {
            server.services().nameToIdCache().resolveOfflineUsers(false);
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(server, name);
        }
        if (uuid == null && CarpetSettings.allowSpawningOfflinePlayers) {
            server.services().nameToIdCache().resolveOfflineUsers(server.isDedicatedServer() && server.usesAuthentication());
            uuid = UUIDUtil.createOfflinePlayerUUID(name);
        }
        if (uuid == null) return null;

        return new GameProfile(uuid, name);
    }

    public static CompletableFuture<GameProfile> fetchGameProfile(MinecraftServer server, GameProfile profile) {
        final ResolvableProfile resolvableProfile = ResolvableProfile.createUnresolved(profile.id());
        return resolvableProfile.resolveProfile(server.services().profileResolver());
    }

}
