package dev.dubhe.gugle.carpet.tools;

import carpet.CarpetSettings;
import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.mixin.APAccessor;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.EntityPlayerMPFakeInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class FakePlayerResident {

    public static JsonObject save(Player player) {
        double pos_x = player.getX();
        double pos_y = player.getY();
        double pos_z = player.getZ();
        double yaw = player.getYRot();
        double pitch = player.getXRot();
        String dimension = player.level().dimension().location().getPath();
        String gamemode = ((ServerPlayer) player).gameMode.getGameModeForPlayer().getName();
        boolean flying = player.getAbilities().flying;
        JsonObject fakePlayer = new JsonObject();
        fakePlayer.addProperty("pos_x", pos_x);
        fakePlayer.addProperty("pos_y", pos_y);
        fakePlayer.addProperty("pos_z", pos_z);
        fakePlayer.addProperty("yaw", yaw);
        fakePlayer.addProperty("pitch", pitch);
        fakePlayer.addProperty("dimension", dimension);
        fakePlayer.addProperty("gamemode", gamemode);
        fakePlayer.addProperty("flying", flying);
        if (GcaSetting.fakePlayerReloadAction) {
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            fakePlayer.add("actions", apToJson(ap));
        }
        return fakePlayer;
    }

    public static void createFake(String username, MinecraftServer server, Vec3 pos, double yaw, double pitch, ResourceKey<Level> dimensionId, GameType gamemode, boolean flying, final JsonObject actions, Runnable onError) {
        ServerLevel worldIn = server.getLevel(dimensionId);
        GameProfileCache.setUsesAuthentication(false);

        GameProfile gameprofile;
        try {
            gameprofile = server.getProfileCache().get(username).orElse(null);
        } finally {
            GameProfileCache.setUsesAuthentication(server.isDedicatedServer() && server.usesAuthentication());
        }

        if (gameprofile == null) {
            if (!CarpetSettings.allowSpawningOfflinePlayers) {
                onError.run();
                return;
            }

            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(username), username);
        }

        GameProfile finalGameprofile = gameprofile;
        EntityPlayerMPFakeInvoker.fetchGameProfile(gameprofile.getName()).thenAccept((p) -> {
            GameProfile current = finalGameprofile;
            if (p.isPresent()) {
                current = p.get();
            }

            EntityPlayerMPFake instance = EntityPlayerMPFakeInvoker.create(server, worldIn, current, ClientInformation.createDefault(), false);
            instance.fixStartingPosition = () -> instance.moveTo(pos.x, pos.y, pos.z, (float) yaw, (float) pitch);
            server.getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), instance, new CommonListenerCookie(current, 0, instance.clientInformation()));
            instance.teleportTo(worldIn, pos.x, pos.y, pos.z, (float) yaw, (float) pitch);
            instance.setHealth(20.0F);
            ((EntityInvoker) instance).unsetRemoved();
            instance.setMaxUpStep(0.6F);
            instance.gameMode.changeGameModeForPlayer(gamemode);
            server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(instance, (byte) ((int) (instance.yHeadRot * 256.0F / 360.0F))), dimensionId);
            server.getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(instance), dimensionId);
            instance.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 127);
            instance.getAbilities().flying = flying;

            apFromJson(actions, instance);
        });
    }

    public static void load(Map.Entry<String, JsonElement> entry, MinecraftServer server) {
        String username = entry.getKey();
        JsonObject fakePlayer = entry.getValue().getAsJsonObject();
        double pos_x = fakePlayer.get("pos_x").getAsDouble();
        double pos_y = fakePlayer.get("pos_y").getAsDouble();
        double pos_z = fakePlayer.get("pos_z").getAsDouble();
        double yaw = fakePlayer.get("yaw").getAsDouble();
        double pitch = fakePlayer.get("pitch").getAsDouble();
        String dimension = fakePlayer.get("dimension").getAsString();
        String gamemode = fakePlayer.get("gamemode").getAsString();
        boolean flying = fakePlayer.get("flying").getAsBoolean();
        JsonObject actions = new JsonObject();
        if (GcaSetting.fakePlayerReloadAction && fakePlayer.has("actions")) {
            actions = fakePlayer.get("actions").getAsJsonObject();
        }
        FakePlayerResident.createFake(username, server, new Vec3(pos_x, pos_y, pos_z), yaw, pitch,
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimension)),
                GameType.byName(gamemode), flying, actions, () -> GcaExtension.LOGGER.error("Not allow spawning offline players!"));
    }

    static JsonObject apToJson(EntityPlayerActionPack ap) {
        JsonObject object = new JsonObject();
        EntityPlayerActionPack.Action attack = ((APAccessor) ap).getActions().get(EntityPlayerActionPack.ActionType.ATTACK);
        EntityPlayerActionPack.Action use = ((APAccessor) ap).getActions().get(EntityPlayerActionPack.ActionType.USE);
        EntityPlayerActionPack.Action jump = ((APAccessor) ap).getActions().get(EntityPlayerActionPack.ActionType.JUMP);
        if (attack != null && !attack.done) object.addProperty("attack", attack.interval);
        if (use != null && !use.done) object.addProperty("use", use.interval);
        if (jump != null && !jump.done) object.addProperty("jump", jump.interval);
        object.addProperty("sneaking", ((APAccessor) ap).getSneaking());
        object.addProperty("sprinting", ((APAccessor) ap).getSprinting());
        object.addProperty("forward", ((APAccessor) ap).getForward());
        object.addProperty("strafing", ((APAccessor) ap).getStrafing());
        return object;
    }

    static void apFromJson(JsonObject actions, ServerPlayer player) {
        EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
        if (actions.has("sneaking")) ap.setSneaking(actions.get("sneaking").getAsBoolean());
        if (actions.has("sprinting")) ap.setSprinting(actions.get("sprinting").getAsBoolean());
        if (actions.has("forward")) ap.setForward(actions.get("forward").getAsFloat());
        if (actions.has("strafing")) ap.setStrafing(actions.get("strafing").getAsFloat());
        if (actions.has("attack"))
            ap.start(EntityPlayerActionPack.ActionType.ATTACK, EntityPlayerActionPack.Action.interval(actions.get("attack").getAsInt()));
        if (actions.has("use"))
            ap.start(EntityPlayerActionPack.ActionType.USE, EntityPlayerActionPack.Action.interval(actions.get("use").getAsInt()));
        if (actions.has("jump"))
            ap.start(EntityPlayerActionPack.ActionType.JUMP, EntityPlayerActionPack.Action.interval(actions.get("jump").getAsInt()));
    }
}
