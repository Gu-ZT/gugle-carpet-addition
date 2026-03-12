package dev.dubhe.gugle.carpet.util;

import carpet.CarpetSettings;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BotUtil {
    public static boolean spawnBot(MinecraftServer server, BotInfo bot)
    {
        ServerLevel level = server.getLevel(bot.dimension());
        GameProfileCache.setUsesAuthentication(false);
        GameProfile gameProfile = getGameProfile(server, bot.name());
        if (gameProfile == null) return false;
        fetchGameProfile(gameProfile.getName()).thenAcceptAsync(p -> {
            GameProfile profile = p.orElse(gameProfile);
            EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, level, profile, ClientInformation.createDefault());
            instance.fixStartingPosition = () -> instance.moveTo(bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
            server.getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), instance, new CommonListenerCookie(profile, 0, instance.clientInformation(), false));
            instance.teleportTo(level, bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
            instance.setHealth(20.0F);
            ((EntityInvoker) instance).invokeUnsetRemoved();
            instance.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6F);
            instance.gameMode.changeGameModeForPlayer(bot.mode());
            server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(instance, (byte) (instance.yHeadRot * 256 / 360)), bot.dimension());
            server.getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(instance), bot.dimension());
            instance.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 0x7f); // show all model layers (incl. capes)
            instance.getAbilities().flying = bot.flying();
            bot.actions().applyAction(instance);
        }, server);
        return true;
    }

    private static GameProfile getGameProfile(MinecraftServer server, final String name) {
        GameProfile gameprofile = null;
        try {
            GameProfileCache cache = server.getProfileCache();
            if (cache != null) {
                gameprofile = server.getProfileCache().get(name).orElse(null);
            }
        }
        finally {
            GameProfileCache.setUsesAuthentication(server.isDedicatedServer() && server.usesAuthentication());
        }
        if (gameprofile == null && CarpetSettings.allowSpawningOfflinePlayers) {
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        }
        return gameprofile;
    }

    private static CompletableFuture<Optional<GameProfile>> fetchGameProfile(final String name) {
        return SkullBlockEntity.fetchGameProfile(name);
    }
}
