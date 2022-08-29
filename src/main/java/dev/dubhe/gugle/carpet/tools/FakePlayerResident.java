package dev.dubhe.gugle.carpet.tools;
import carpet.patches.EntityPlayerMPFake;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.Map;

public class FakePlayerResident {

    public static JsonObject save(Player player) {
        double pos_x = player.getX();
        double pos_y = player.getY();
        double pos_z = player.getZ();
        double yaw = player.getYRot();
        double pitch = player.getXRot();
        String dimension = player.level.dimension().location().getPath();
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
        return fakePlayer;
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
        EntityPlayerMPFake.createFake(username, server, pos_x, pos_y, pos_z, yaw, pitch,
                ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimension)),
                GameType.byName(gamemode), flying);
    }
}
