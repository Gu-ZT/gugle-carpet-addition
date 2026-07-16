package dev.dubhe.gugle.carpet.tools.player;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.commands.BotCommand;
import dev.dubhe.gugle.carpet.entry.BotActionInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.util.BotUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FakePlayerAutoRespawn {
    private static final Map<UUID, EntityPlayerActionPack> cachedDiedFakePlayers = new HashMap<>();

    public static void onFakePlayerDied(UUID uuid, EntityPlayerActionPack actionPack) {
        if ("false".equals(GcaSetting.fakePlayerAutoRespawn)) return;
        cachedDiedFakePlayers.put(uuid, actionPack);
    }

    public static void onFakePlayerSpawned(UUID uuid) {
        cachedDiedFakePlayers.remove(uuid);
    }

    public static void tryRespawn(EntityPlayerMPFake player) {
        EntityPlayerActionPack pack = cachedDiedFakePlayers.remove(player.getUUID());
        if (pack == null || "false".equals(GcaSetting.fakePlayerAutoRespawn)) return;

        EntityPlayerActionPack actionPack = ((ServerPlayerInterface) player).getActionPack();
        MinecraftServer server = player.level().getServer();
        assert server != null;
        GameProfile profile = player.getGameProfile();
        String name = profile.getName();

        BotInfo respawnBot = getRespawnBotInfo(player, server, name);

        server.tell(new TickTask(server.getTickCount() + 1, () -> {
            BotUtil.spawnBot(server, null, respawnBot, profile, GcaSetting.fakePlayerReloadAction, actionPack);
        }));
    }

    private static BotInfo getRespawnBotInfo(EntityPlayerMPFake player, MinecraftServer server, String name) {
        if ("setting".equals(GcaSetting.fakePlayerAutoRespawn)) {
            BotInfo botInfo = BotCommand.getBotInfo(server, name);
            if (botInfo != null) return botInfo;
        }

        DimensionTransition transition = getRespawnPosition(player);
        Vec2 facing = new Vec2(transition.xRot(), transition.yRot());

        return new BotInfo(
            name,
            "",
            transition.pos(),
            facing,
            transition.newLevel().dimension(),
            player.gameMode.getGameModeForPlayer(),
            player.getAbilities().flying,
            BotActionInfo.EMPTY,
            List.of()
        );
    }

    private static DimensionTransition getRespawnPosition(EntityPlayerMPFake player) {
        if ("spawn".equals(GcaSetting.fakePlayerAutoRespawn)) {
            return player.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
        }

        ServerLevel level = (ServerLevel) player.level();
        float xRot = player.getXRot();
        float yRot = player.getYRot();

        return new DimensionTransition(level, player.position(), Vec3.ZERO, yRot, xRot, DimensionTransition.DO_NOTHING);
    }
}
