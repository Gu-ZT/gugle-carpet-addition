package dev.dubhe.gugle.carpet.util;

import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.entry.BotExecutorInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.List;

public class BotSpawnUtil {
    public static boolean spawnBot(MinecraftServer server, BotInfo bot) {
        return spawnBot(server, bot, true);
    }

    public static boolean spawnBot(MinecraftServer server, BotInfo bot, boolean applyAction) {
        ServerLevel level = server.getLevel(bot.dimension());
        GameProfile gameProfile = GameProfileUtil.getGameProfile(server, bot.name());
        if (gameProfile == null || level == null) return false;

        return spawnBot(server, level, bot, gameProfile, applyAction, null);
    }

    public static boolean spawnBot(MinecraftServer server, @Nullable ServerLevel level, BotInfo preBot, GameProfile profile, boolean applyAction, @Nullable EntityPlayerActionPack actionPack) {
        if (level == null) {
            level = server.getLevel(preBot.dimension());
            if (level == null) return false;
        }

        EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, level, profile);
        BotInfo bot = preBot.pos() == null ? preBot.withPos(instance.position()) : preBot;
        instance.fixStartingPosition = () -> instance.moveTo(bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
        server.getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), instance);
        instance.teleportTo(level, bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
        instance.setHealth(20.0F);
        ((EntityInvoker) instance).invokeUnsetRemoved();
        instance.setMaxUpStep(0.6F);
        instance.gameMode.changeGameModeForPlayer(bot.mode());
        server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(instance, (byte) (instance.yHeadRot * 256 / 360)), bot.dimension());
        server.getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(instance), bot.dimension());
        instance.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 0x7f); // show all model layers (incl. capes)
        instance.getAbilities().flying = bot.flying();
        if (applyAction) applyAction(server, instance, bot, actionPack);
        return true;
    }

    private static void applyAction(MinecraftServer server, EntityPlayerMPFake instance, BotInfo bot, @Nullable EntityPlayerActionPack actionPack) {
        List<BotExecutorInfo> startups = bot.getStartups();
        if (startups.isEmpty()) {
            bot.actions().applyAction(instance, actionPack);
            return;
        }

        for (BotExecutorInfo startup : startups) {
            try {
                server.getCommands().getDispatcher().execute(
                    startup.command(bot.name()).substring(1),
                    instance.createCommandSourceStack()
                );
            } catch (CommandSyntaxException e) {
                GcaExtension.LOGGER.warn("Failed to execute startup action {} for bot {}: {}", startup.desc(), bot.name(), e.getMessage());
            }
        }
    }


}
