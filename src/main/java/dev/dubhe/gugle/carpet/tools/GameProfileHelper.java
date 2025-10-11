package dev.dubhe.gugle.carpet.tools;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import org.jetbrains.annotations.NotNull;
import com.mojang.authlib.GameProfile;
//#if MC>=12109
//$$ import net.minecraft.world.item.component.ResolvableProfile;
//$$ import net.minecraft.server.players.NameAndId;
//$$ import java.util.concurrent.CompletableFuture;
//#endif

import java.util.UUID;


public class GameProfileHelper {
    public static String prasePlayerGameName(@NotNull ServerPlayer player) {
        //#if MC<12109
        return player.getGameProfile().getName();
        //#else
        //$$ return player.nameAndId().name();
        //#endif
    }

    public static UUID prasePlayerGameID(@NotNull ServerPlayer player) {
        //#if MC<12109
        return player.getGameProfile().getId();
        //#else
        //$$ return player.nameAndId().id();
        //#endif
    }

    public static @NotNull
    //#if MC<12109
    GameProfile
    //#else
    //$$ NameAndId
    //#endif
    prasePlayerGameProfile(@NotNull ServerPlayer player) {
        //#if MC<12109
        return player.getGameProfile();
        //#else
        //$$ return player.nameAndId();
        //#endif
    }

    public static void prasePlayerGameProfile(
        @NotNull ServerPlayer player,
        @NotNull TriConsumer<
            //#if MC<12109
            GameProfile,
            //#else
            //$$ NameAndId,
            //#endif
            String,
            UUID
            > consumer
    ) {
        //#if MC<12109
        GameProfile profile = player.getGameProfile();
        //#else
        //$$ NameAndId profile = player.nameAndId();
        //#endif
        consumer.accept(
            profile,
            //#if MC<12109
            profile.getName(),
            profile.getId()
            //#else
            //$$ profile.name(),
            //$$ profile.id()
            //#endif
        );
    }

    public static void praseGameProfileCollection(
        CommandContext<CommandSourceStack> context,
        String name,
        TriConsumer<
            //#if MC<12109
            GameProfile,
            //#else
            //$$ NameAndId,
            //#endif
            String,
            UUID
            > consumer
    ) throws CommandSyntaxException {
        for (
            //#if MC<12109
            GameProfile profile
            //#else
            //$$ NameAndId profile
            //#endif
            : GameProfileArgument.getGameProfiles(context, name)
        ) {
            consumer.accept(
                profile,
                //#if MC<12109
                profile.getName(),
                profile.getId()
                //#else
                //$$ profile.name(),
                //$$ profile.id()
                //#endif
            );
        }
    }

    public static MinecraftServer getServerPlayerServer(@NotNull ServerPlayer player) {
        //#if MC<12109
        return player.getServer();
        //#else
        //$$ return player.level().getServer();
        //#endif
    }

    public static GameProfileCache getProfileCache(@NotNull MinecraftServer server) {
        //#if MC<12109
        return server.getProfileCache();
        //#else
        //$$ return (CachedUserNameToIdResolver) server.services().nameToIdCache();
        //#endif
    }

    //#if MC>=12109
    //$$ public static CompletableFuture<GameProfile> fetchGameProfile(@NotNull MinecraftServer server, UUID name) {
    //$$     ResolvableProfile resolvableProfile = ResolvableProfile.createUnresolved(name);
    //$$     return resolvableProfile.resolveProfile(server.services().profileResolver());
    //$$ }
    //#endif
}
