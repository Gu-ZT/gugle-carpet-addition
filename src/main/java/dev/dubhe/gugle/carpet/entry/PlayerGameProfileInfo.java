package dev.dubhe.gugle.carpet.entry;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PlayerGameProfileInfo(@Nullable ServerPlayer player, GameProfile profile, String name, UUID uuid) {

    public static PlayerGameProfileInfo of(ServerPlayer player) {
        GameProfile profile = player.getGameProfile();
        return new PlayerGameProfileInfo(player, profile, profile.getName(), profile.getId());
    }

    public static PlayerGameProfileInfo of(GameProfile profile) {
        return new PlayerGameProfileInfo(null, profile, profile.getName(), profile.getId());
    }

}
