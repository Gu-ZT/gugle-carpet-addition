package dev.dubhe.gugle.carpet.util;

import carpet.CarpetSettings;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import dev.dubhe.gugle.carpet.tools.GameProfileHelper;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import org.jetbrains.annotations.Nullable;

public class BotUtil {
    public static boolean spawnBot(CommandSourceStack source, @Nullable BotInfo bot) {
        if (bot == null) {
            source.sendFailure(Component.literal("%s is not exist."));
            return false;
        }
        if (source.getServer().getPlayerList().getPlayerByName(bot.name()) != null) {
            source.sendFailure(Component.literal("player %s is already exist.".formatted(bot.name())));
            return false;
        }
        source.getServer().getLevel(bot.dimType());
        try {
            ServerLevel worldIn = source.getServer().getLevel(bot.dimType());
            if (worldIn == null) return false;
            GameProfileCache.setUsesAuthentication(false);
            try {
                GameProfile gameprofile = getGameProfile(source.getServer(), bot.name());
                if (gameprofile == null) return false;
                EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(source.getServer(), worldIn, gameprofile);
                instance.fixStartingPosition = () -> instance.moveTo(bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
                source.getServer().getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), instance);
                instance.teleportTo(worldIn, bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
                instance.setHealth(20.0F);
                ((EntityInvoker) instance).invokeUnsetRemoved();
                instance.setMaxUpStep(0.6F);
                instance.gameMode.changeGameModeForPlayer(bot.mode());
                source.getServer().getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(instance, (byte)((int)(instance.yHeadRot * 256.0F / 360.0F))), bot.dimType());
                source.getServer().getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(instance), bot.dimType());
                instance.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte)127);
                instance.getAbilities().flying = bot.flying();
                FakePlayerSerializer.applyActionPackFromJson(bot.actions(), instance);
            } finally {
                GameProfileCache.setUsesAuthentication(source.getServer().isDedicatedServer() && source.getServer().usesAuthentication());
            }
            return true;
        } catch (Exception e) {
            GcaExtension.LOGGER.error("Failed to load bot: {}", bot.name(), e);
            source.sendFailure(Component.literal("%s is not loaded.".formatted(bot.name())));
            return false;
        }
    }

    @Nullable
    private static GameProfile getGameProfile(MinecraftServer server, String name) {
        GameProfileCache cache = GameProfileHelper.getProfileCache(server);
        GameProfile gameprofile = cache == null ? null : cache.get(name).orElse(null);
        if (gameprofile == null && CarpetSettings.allowSpawningOfflinePlayers) {
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        }
        return gameprofile;
    }
}
