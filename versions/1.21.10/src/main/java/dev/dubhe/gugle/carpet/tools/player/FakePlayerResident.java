package dev.dubhe.gugle.carpet.tools.player;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.EntityPlayerMPFakeInvoker;
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
        return fakePlayer;
    }

    public static void createFake(
        String username,
        @NotNull MinecraftServer server,
        final JsonObject actions
    ) {
        GameProfileHelper.fetchGameProfile(server, username)
            .thenAcceptAsync((profile) -> {
                EntityPlayerMPFake playerMPFake = EntityPlayerMPFake.respawnFake(
                    server,
                    server.overworld(),
                    profile,
                    ClientInformation.createDefault()
                );
                server.getPlayerList().placeNewPlayer(
                    new FakeClientConnection(PacketFlow.SERVERBOUND),
                    playerMPFake,
                    new CommonListenerCookie(profile, 0, playerMPFake.clientInformation(), false)
                );
                playerMPFake.setHealth(20.0F);
                AttributeInstance attribute = playerMPFake.getAttribute(Attributes.STEP_HEIGHT);
                if (attribute != null) attribute.setBaseValue(0.6F);
                server.getPlayerList().broadcastAll(
                    new ClientboundRotateHeadPacket(playerMPFake, ((byte) (playerMPFake.yHeadRot * 256.0F / 360.0F))),
                    playerMPFake.level().dimension()
                );
                EntityPlayerMPFakeInvoker.invokeLoadPlayerData(playerMPFake);
                server.getPlayerList().broadcastAll(ClientboundEntityPositionSyncPacket.of(playerMPFake), playerMPFake.level().dimension());
                playerMPFake.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 127);

                FakePlayerSerializer.applyActionPackFromJson(actions, playerMPFake);
                ((EntityInvoker) playerMPFake).invokeUnsetRemoved();
            }, server);
    }

    public static void load(Map.@NotNull Entry<String, JsonElement> entry, MinecraftServer server) {
        String username = entry.getKey();
        JsonObject fakePlayer = entry.getValue().getAsJsonObject();
        JsonObject actions = new JsonObject();
        if (GcaSetting.fakePlayerReloadAction && fakePlayer.has("actions")) {
            actions = fakePlayer.get("actions").getAsJsonObject();
        }
        FakePlayerResident.createFake(username, server, actions);
    }
}
