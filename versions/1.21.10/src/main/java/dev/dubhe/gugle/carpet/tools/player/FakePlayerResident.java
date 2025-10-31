package dev.dubhe.gugle.carpet.tools.player;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import dev.dubhe.gugle.carpet.tools.GameProfileHelper;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FakePlayerResident {
    public static @NotNull JsonObject save(Player player) {
        JsonObject fakePlayer = new JsonObject();
        if (GcaSetting.fakePlayerReloadAction) {
            EntityPlayerActionPack actionPack = ((ServerPlayerInterface) player).getActionPack();
            fakePlayer.add("actions", FakePlayerSerializer.actionPackToJson(actionPack));
        }
        JsonArray pos = new JsonArray();
        Vec3 position = player.position();
        Vec2 rotation = player.getRotationVector();
        pos.add(position.x);
        pos.add(position.y);
        pos.add(position.z);
        pos.add(rotation.x);
        pos.add(rotation.y);
        fakePlayer.add("pos", pos);
        return fakePlayer;
    }

    public static void createFake(
        String username,
        @NotNull MinecraftServer server,
        final JsonObject actions,
        Vec3 position,
        Vec2 rotation
    ) {
        GameProfileHelper.fetchGameProfile(server, username)
            .thenAcceptAsync((profile) -> {
                EntityPlayerMPFake playerMPFake = EntityPlayerMPFake.respawnFake(server, server.overworld(), profile, ClientInformation.createDefault());
                server.getPlayerList().placeNewPlayer(
                    new FakeClientConnection(PacketFlow.SERVERBOUND),
                    playerMPFake,
                    new CommonListenerCookie(profile, 0, playerMPFake.clientInformation(), false)
                );
                if (position != null) {
                    playerMPFake.snapTo(position);
                }
                if (rotation != null) {
                    playerMPFake.setXRot(rotation.x);
                    playerMPFake.setYRot(rotation.y);
                }
                playerMPFake.setHealth(20.0F);
                AttributeInstance attribute = playerMPFake.getAttribute(Attributes.STEP_HEIGHT);
                if (attribute != null) attribute.setBaseValue(0.6F);
                server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(playerMPFake, ((byte) (playerMPFake.yHeadRot * 256.0F / 360.0F))), playerMPFake.level().dimension());
                server.getPlayerList().broadcastAll(ClientboundEntityPositionSyncPacket.of(playerMPFake), playerMPFake.level().dimension());
                playerMPFake.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 127);

                FakePlayerSerializer.applyActionPackFromJson(actions, playerMPFake);
                ((EntityInvoker) playerMPFake).invokerUnsetRemoved();
            }, server);
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
