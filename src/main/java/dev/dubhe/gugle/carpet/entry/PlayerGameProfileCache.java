package dev.dubhe.gugle.carpet.entry;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PlayerGameProfileCache(@Nullable ServerPlayer player, GameProfile profile, String name, UUID uuid) {

    public static PlayerGameProfileCache of(ServerPlayer player) {
        GameProfile profile = player.getGameProfile();
        return new PlayerGameProfileCache(player, profile, profile.getName(), profile.getId());
    }

    public static PlayerGameProfileCache of(GameProfile profile) {
        return new PlayerGameProfileCache(null, profile, profile.getName(), profile.getId());
    }

}
