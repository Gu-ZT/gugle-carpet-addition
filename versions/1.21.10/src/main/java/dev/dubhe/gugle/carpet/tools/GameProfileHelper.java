package dev.dubhe.gugle.carpet.tools;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.UUID;

public class GameProfileHelper {
    public static String prasePlayerGameName(ServerPlayer player) {
        return player.nameAndId().name();
    }

    public static UUID prasePlayerGameID(ServerPlayer player) {
        return player.nameAndId().id();
    }

    public static NameAndId prasePlayerGameProfile(ServerPlayer player) {
        return player.nameAndId();
    }

    public static void prasePlayerGameProfile(ServerPlayer player, TriConsumer<NameAndId, String, UUID> consumer) {
        NameAndId profile = player.nameAndId();
        consumer.accept(profile, profile.name(), profile.id());
    }

    public static void praseGameProfileCollection(CommandContext<CommandSourceStack> context, String name, TriConsumer<NameAndId, String, UUID> consumer) throws CommandSyntaxException {
        for (NameAndId profile : GameProfileArgument.getGameProfiles(context, name)) {
            consumer.accept(profile, profile.name(), profile.id());
        }
    }

    public static MinecraftServer getServerPlayerServer(ServerPlayer player) {
        return player.level().getServer();
    }

    public static @Nullable CachedUserNameToIdResolver getProfileCache(MinecraftServer server) {
        return (CachedUserNameToIdResolver) server.services().nameToIdCache();
    }

    public static CompletableFuture<GameProfile> fetchGameProfile(MinecraftServer server, UUID name) {
        ResolvableProfile resolvableProfile = ResolvableProfile.createUnresolved(name);
        return resolvableProfile.resolveProfile(server.services().profileResolver());
    }

    public static CompletableFuture<GameProfile> fetchGameProfile(MinecraftServer server, String name) {
        ResolvableProfile resolvableProfile = ResolvableProfile.createUnresolved(name);
        return resolvableProfile.resolveProfile(server.services().profileResolver());
    }
}
