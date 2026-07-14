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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
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

    public static void onFakePlayerSpawned(UUID uuid) {
        cachedDiedFakePlayers.remove(uuid);
    }

    public static void tryRespawn(EntityPlayerMPFake player) {
        EntityPlayerActionPack pack = cachedDiedFakePlayers.remove(player.getUUID());
        if (pack == null || "false".equals(GcaSetting.fakePlayerAutoRespawn)) return;

        EntityPlayerActionPack actionPack = ((ServerPlayerInterface) player).getActionPack();
        MinecraftServer server = player.server;
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

        PortalDimensionInfo transition = getRespawnPosition(player);
        Vec2 facing = new Vec2(transition.xRot, transition.yRot);

        return new BotInfo(
            name,
            "",
            transition.pos,
            facing,
            transition.dimension,
            player.gameMode.getGameModeForPlayer(),
            player.getAbilities().flying,
            BotActionInfo.EMPTY,
            List.of()
        );
    }

    private static PortalDimensionInfo getRespawnPosition(EntityPlayerMPFake player) {
        float xRot = player.getXRot();
        float yRot = player.getYRot();

        if ("normal".equals(GcaSetting.fakePlayerAutoRespawn)) {
            ServerLevel serverLevel = player.server.getLevel(player.getRespawnDimension());
            BlockPos respawnPos = player.getRespawnPosition();

            Optional<Vec3> optional;
            if (serverLevel != null && respawnPos != null) {
                optional = Player.findRespawnPositionAndUseSpawnBlock(
                    serverLevel,
                    respawnPos,
                    player.getRespawnAngle(),
                    player.isRespawnForced(),
                    false
                );
            } else {
                optional = Optional.empty();
            }

            ServerLevel level = serverLevel != null && optional.isPresent() ?
                serverLevel :
                player.server.overworld();

            Vec3 pos = optional.orElse(Vec3.atBottomCenterOf(level.getSharedSpawnPos()));

            return new PortalDimensionInfo(level.dimension(), pos, Vec3.ZERO, yRot, xRot);
        }

        ResourceKey<Level> dimension = player.level().dimension();
        return new PortalDimensionInfo(dimension, player.position(), Vec3.ZERO, yRot, xRot);
    }

    private static class PortalDimensionInfo extends PortalInfo {
        private final ResourceKey<Level> dimension;

        public PortalDimensionInfo(ResourceKey<Level> dimension, Vec3 pos, Vec3 vec32, float f, float g) {
            super(pos, vec32, f, g);
            this.dimension = dimension;
        }
    }

}
