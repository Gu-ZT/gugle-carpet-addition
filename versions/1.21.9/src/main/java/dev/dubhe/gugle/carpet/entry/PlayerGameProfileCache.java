package dev.dubhe.gugle.carpet.entry;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.UUID;

public record PlayerGameProfileCache(ServerPlayer player, NameAndId profile, String name, UUID uuid) {

    public static PlayerGameProfileCache of(ServerPlayer player) {
        NameAndId profile = player.nameAndId();
        return new PlayerGameProfileCache(player, profile, profile.name(), profile.id());
    }

    public static PlayerGameProfileCache of(NameAndId profile) {
        return new PlayerGameProfileCache(null, profile, profile.name(), profile.id());
    }

}
