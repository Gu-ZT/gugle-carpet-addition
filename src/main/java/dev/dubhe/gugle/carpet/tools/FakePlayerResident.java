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
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class FakePlayerResident {
    public static JsonObject save(Player player) {
        JsonObject fakePlayer = new JsonObject();
        if (GcaSetting.fakePlayerReloadAction) {
            EntityPlayerActionPack actionPack = ((ServerPlayerInterface) player).getActionPack();
            fakePlayer.add("actions", actionPackToJson(actionPack));
        }
        return fakePlayer;
    }

    public static void createFake(String username, MinecraftServer server, final JsonObject actions) {
        GameProfileCache.setUsesAuthentication(false);
        GameProfile gameprofile;
        try {
            GameProfileCache profileCache = server.getProfileCache();
            if (profileCache == null) {
                return;
            }
            gameprofile = profileCache.get(username).orElse(null);
        } finally {
            GameProfileCache.setUsesAuthentication(server.isDedicatedServer() && server.usesAuthentication());
        }
        if (gameprofile == null) {
            if (!CarpetSettings.allowSpawningOfflinePlayers) {
                GcaExtension.LOGGER.error("Spawning offline players %s is not allowed!".formatted(username));
                return;
            }
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(username), username);
        }
        GameProfile finalGameprofile = gameprofile;
        EntityPlayerMPFakeInvoker.invokerFetchGameProfile(gameprofile.getName()).thenAcceptAsync((p) -> {
            GameProfile current = finalGameprofile;
            if (p.isPresent()) {
                current = p.get();
            }
            EntityPlayerMPFake playerMPFake = EntityPlayerMPFake.respawnFake(server, server.overworld(), current, ClientInformation.createDefault());
            server.getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), playerMPFake,
                    new CommonListenerCookie(current, 0, playerMPFake.clientInformation(), false));
            playerMPFake.setHealth(20.0F);
            playerMPFake.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6F);
            server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(playerMPFake, ((byte) (playerMPFake.yHeadRot * 256.0F / 360.0F))), playerMPFake.serverLevel().dimension());
            server.getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(playerMPFake), playerMPFake.serverLevel().dimension());
            playerMPFake.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 127);

            applyActionPackFromJson(actions, playerMPFake);
            ((EntityInvoker) playerMPFake).invokerUnsetRemoved();
        }, server);
    }

    public static void load(Map.Entry<String, JsonElement> entry, MinecraftServer server) {
        String username = entry.getKey();
        JsonObject fakePlayer = entry.getValue().getAsJsonObject();
        JsonObject actions = new JsonObject();
        if (GcaSetting.fakePlayerReloadAction && fakePlayer.has("actions")) {
            actions = fakePlayer.get("actions").getAsJsonObject();
        }
        FakePlayerResident.createFake(username, server, actions);
    }

    static JsonObject actionPackToJson(EntityPlayerActionPack actionPack) {
        JsonObject object = new JsonObject();
        EntityPlayerActionPack.Action attack = ((APAccessor) actionPack).getActions().get(EntityPlayerActionPack.ActionType.ATTACK);
        EntityPlayerActionPack.Action use = ((APAccessor) actionPack).getActions().get(EntityPlayerActionPack.ActionType.USE);
        EntityPlayerActionPack.Action jump = ((APAccessor) actionPack).getActions().get(EntityPlayerActionPack.ActionType.JUMP);
        if (attack != null && !attack.done) {
            object.addProperty("attack", attack.interval);
        }
        if (use != null && !use.done) {
            object.addProperty("use", use.interval);
        }
        if (jump != null && !jump.done) {
            object.addProperty("jump", jump.interval);
        }
        object.addProperty("sneaking", ((APAccessor) actionPack).getSneaking());
        object.addProperty("sprinting", ((APAccessor) actionPack).getSprinting());
        object.addProperty("forward", ((APAccessor) actionPack).getForward());
        object.addProperty("strafing", ((APAccessor) actionPack).getStrafing());
        return object;
    }

    static void applyActionPackFromJson(JsonObject actions, ServerPlayer player) {
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
