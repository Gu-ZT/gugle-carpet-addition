package dev.dubhe.gugle.carpet.tools.player;

import carpet.CarpetSettings;
import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import dev.dubhe.gugle.carpet.tools.GameProfileHelper;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
//#if MC>=12102
//$$ import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
//#else
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
//#endif
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


//#if MC>=12100
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
//#else
//#endif

public class FakePlayerResident {
    public static @NotNull JsonObject save(Player player) {
        JsonObject fakePlayer = new JsonObject();
        if (GcaSetting.fakePlayerReloadAction) {
            EntityPlayerActionPack actionPack = ((ServerPlayerInterface) player).getActionPack();
            fakePlayer.add("actions", FakePlayerSerializer.actionPackToJson(actionPack));
        }
        return fakePlayer;
    }

    public static void createFake(
        String username,
        @NotNull MinecraftServer server,
        final JsonObject actions,
        Vec3 position,
        Vec2 rotation
    ) {
        GameProfileCache.setUsesAuthentication(false);
        GameProfile gameprofile;
        try {
            GameProfileCache profileCache = GameProfileHelper.getProfileCache(server);
            if (profileCache == null) {
                return;
            }
            gameprofile = profileCache.get(username).orElse(null);
        } finally {
            GameProfileCache.setUsesAuthentication(server.isDedicatedServer() && server.usesAuthentication());
        }
        if (gameprofile == null) {
            if (!CarpetSettings.allowSpawningOfflinePlayers) {
                GcaExtension.LOGGER.error("Spawning offline players {} is not allowed!", username);
                return;
            }
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(username), username);
        }
        //#if MC>=12100
        GameProfile finalGameprofile = gameprofile;
        SkullBlockEntity.fetchGameProfile(gameprofile.getName()).thenAcceptAsync((p) -> {
            GameProfile current = finalGameprofile;
            if (p.isPresent()) {
                current = p.get();
            }
            EntityPlayerMPFake playerMPFake = EntityPlayerMPFake.respawnFake(server, server.overworld(), current, ClientInformation.createDefault());
            server.getPlayerList().placeNewPlayer(
                new FakeClientConnection(PacketFlow.SERVERBOUND),
                playerMPFake,
                new CommonListenerCookie(current, 0, playerMPFake.clientInformation(), false)
            );
            if (position != null) {
                playerMPFake.moveTo(position);
            }
            if (rotation != null) {
                playerMPFake.setXRot(rotation.x);
                playerMPFake.setYRot(rotation.y);
            }
            playerMPFake.setHealth(20.0F);
            AttributeInstance attribute = playerMPFake.getAttribute(Attributes.STEP_HEIGHT);
            if (attribute != null) attribute.setBaseValue(0.6F);
            server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(playerMPFake, ((byte) (playerMPFake.yHeadRot * 256.0F / 360.0F))), playerMPFake.level().dimension());
            //#if MC>=12102
            //$$ server.getPlayerList().broadcastAll(ClientboundEntityPositionSyncPacket.of(playerMPFake), playerMPFake.level().dimension());
            //#else
            server.getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(playerMPFake), playerMPFake.level().dimension());
            //#endif
            playerMPFake.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 127);

            FakePlayerSerializer.applyActionPackFromJson(actions, playerMPFake);
            ((EntityInvoker) playerMPFake).invokeUnsetRemoved();
        }, server);
        //#else
        //$$ EntityPlayerMPFake playerMPFake = EntityPlayerMPFake.respawnFake(server, server.overworld(), gameprofile);
        //$$ server.getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), playerMPFake);
        //$$ playerMPFake.setHealth(20.0F);
        //$$ playerMPFake.setMaxUpStep(0.6F);
        //$$ server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(playerMPFake, ((byte) (playerMPFake.yHeadRot * 256.0F / 360.0F))), playerMPFake.level().dimension());
        //$$ server.getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(playerMPFake), playerMPFake.level().dimension());
        //$$ playerMPFake.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 127);
        //$$ FakePlayerSerializer.applyActionPackFromJson(actions, playerMPFake);
        //$$ ((EntityInvoker) playerMPFake).invokeUnsetRemoved();
        //#endif
    }

    public static void load(Map.@NotNull Entry<String, JsonElement> entry, MinecraftServer server) {
        String username = entry.getKey();
        JsonObject fakePlayer = entry.getValue().getAsJsonObject();
        JsonObject actions = new JsonObject();
        if (GcaSetting.fakePlayerReloadAction && fakePlayer.has("actions")) {
            actions = fakePlayer.get("actions").getAsJsonObject();
        }
        Vec3 position = null;
        Vec2 rotation = null;
        if (fakePlayer.has("pos")) {
            JsonArray pos = fakePlayer.get("pos").getAsJsonArray();
            position = new Vec3(pos.get(0).getAsDouble(), pos.get(1).getAsDouble(), pos.get(2).getAsDouble());
            if (pos.size() > 3) {
                rotation = new Vec2(pos.get(3).getAsFloat(), pos.get(4).getAsFloat());
            }
        }
        GcaExtension.LOGGER.info(
            "Load fake player {} at [{}, {}, {}]",
            username,
            position == null ? "null" : position.x,
            position == null ? "null" : position.y,
            position == null ? "null" : position.z
        );
        FakePlayerResident.createFake(username, server, actions, position, rotation);
    }
}
