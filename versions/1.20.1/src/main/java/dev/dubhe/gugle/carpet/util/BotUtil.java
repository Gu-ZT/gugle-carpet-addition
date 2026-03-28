package dev.dubhe.gugle.carpet.util;

import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class BotUtil {
    public static boolean spawnBot(MinecraftServer server, BotInfo bot) {
        return spawnBot(server, bot, true, false);
    }

    public static boolean spawnBot(MinecraftServer server, BotInfo bot, boolean applyAction, boolean isRespawn) {
        ServerLevel level = server.getLevel(bot.dimension());
        GameProfile gameProfile = GameProfileUtil.getGameProfile(server, bot.name());
        if (gameProfile == null || level == null) return false;

        EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, level, gameProfile);
        instance.fixStartingPosition = () -> instance.moveTo(bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
        server.getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), instance);
        if (!isRespawn) instance.stopRiding();
        instance.teleportTo(level, bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
        instance.setHealth(20.0F);
        instance.unsetRemoved();
        instance.setMaxUpStep(0.6F);
        instance.gameMode.changeGameModeForPlayer(bot.mode());
        server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(instance, (byte) (instance.yHeadRot * 256 / 360)), bot.dimension());
        server.getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(instance), bot.dimension());
        instance.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 0x7f); // show all model layers (incl. capes)
        instance.getAbilities().flying = bot.flying();
        if (applyAction) bot.actions().applyAction(instance);
        return true;
    }

}
