package dev.dubhe.gugle.carpet.entry;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.UUID;

public record PlayerGameProfileInfo(ServerPlayer player, NameAndId profile, String name, UUID uuid) {

    public static PlayerGameProfileInfo of(ServerPlayer player) {
        NameAndId profile = player.nameAndId();
        return new PlayerGameProfileInfo(player, profile, profile.name(), profile.id());
    }

    public static PlayerGameProfileInfo of(NameAndId profile) {
        return new PlayerGameProfileInfo(null, profile, profile.name(), profile.id());
    }

}
