package dev.dubhe.gugle.carpet.tools.player;

import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.commands.BotCommand;
import dev.dubhe.gugle.carpet.entry.BotActionInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.util.BotUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakePlayerAutoRespawn {
    private static final Map<UUID, EntityPlayerActionPack> cachedDiedFakePlayers = new HashMap<>();

    public static void onFakePlayerDied(UUID uuid, EntityPlayerActionPack actionPack) {
        if ("false".equals(GcaSetting.fakePlayerAutoRespawn)) return;
        cachedDiedFakePlayers.put(uuid, actionPack);
    }

    public static void onFakePlayerRespawn(UUID uuid) {
        cachedDiedFakePlayers.remove(uuid);
    }

    public static void tryRespawn(EntityPlayerMPFake player) {
        EntityPlayerActionPack pack = cachedDiedFakePlayers.remove(player.getUUID());
        if (pack == null || "false".equals(GcaSetting.fakePlayerAutoRespawn)) return;

        MinecraftServer server = player.level().getServer();
        GameProfile profile = player.getGameProfile();
        String name = profile.getName();

        BotInfo respawnBot = null;
        if ("setting".equals(GcaSetting.fakePlayerAutoRespawn)) {
            respawnBot = BotCommand.getBotInfo(server, name);
        }

        if (respawnBot == null) {
            DimensionTransition transition = getRespawnPosition(player);
            Vec2 facing = new Vec2(transition.xRot(), transition.yRot());

            respawnBot = new BotInfo(name, "", transition.pos(), facing, transition.newLevel().dimension(), player.gameMode.getGameModeForPlayer(), player.getAbilities().flying, BotActionInfo.EMPTY, List.of());
        }

        final BotInfo finalBot = respawnBot;
        //#if MC < 12102
        server.tell(new TickTask(server.getTickCount() + 1, () -> {
            BotUtil.spawnBot(server, finalBot, false);
        }));
        //#else
        //$$ server.schedule(new TickTask(server.getTickCount() + 1, () -> {
        //$$     BotUtil.spawnBot(server, finalBot, false);
        //$$ }));
        //#endif
    }

    private static DimensionTransition getRespawnPosition(EntityPlayerMPFake player) {
        if ("normal".equals(GcaSetting.fakePlayerAutoRespawn)) {
            return player.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
        }

        ServerLevel level = (ServerLevel) player.level();
        float xRot = player.getXRot();
        float yRot = player.getYRot();

        Optional<GlobalPos> optional = player.getLastDeathLocation();
        if (optional.isPresent()) {
            Vec3 pos = optional.get().pos().getBottomCenter();
            return new DimensionTransition(level, pos, Vec3.ZERO, yRot, xRot, DimensionTransition.DO_NOTHING);
        }

        return new DimensionTransition(level, player.position(), Vec3.ZERO, yRot, xRot, DimensionTransition.DO_NOTHING);
    }
}
