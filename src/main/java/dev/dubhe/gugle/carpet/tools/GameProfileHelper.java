package dev.dubhe.gugle.carpet.tools;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class GameProfileHelper {
    public static String prasePlayerGameName(ServerPlayer player) {
        return player.getGameProfile().getName();
    }

    public static UUID prasePlayerGameID(ServerPlayer player) {
        return player.getGameProfile().getId();
    }

    public static GameProfile prasePlayerGameProfile(ServerPlayer player) {
        return player.getGameProfile();
    }

    public static void prasePlayerGameProfile(ServerPlayer player, TriConsumer<GameProfile, String, UUID> consumer) {
        GameProfile profile = player.getGameProfile();
        consumer.accept(profile, profile.getName(), profile.getId());
    }

    public static void praseGameProfileCollection(
        CommandContext<CommandSourceStack> context,
        String name,
        TriConsumer<GameProfile, String, UUID> consumer
    ) throws CommandSyntaxException {
        for (GameProfile profile : GameProfileArgument.getGameProfiles(context, name)) {
            consumer.accept(profile, profile.getName(), profile.getId());
        }
    }

    public static MinecraftServer getServerPlayerServer(ServerPlayer player) {
        return player.getServer();
    }

    public static GameProfileCache getProfileCache(MinecraftServer server) {
        return server.getProfileCache();
    }
}
