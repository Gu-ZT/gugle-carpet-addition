package dev.dubhe.gugle.carpet;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.patches.EntityPlayerMPFake;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.tools.FakePlayerInventoryContainer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class GcaExtension implements CarpetExtension, ModInitializer {

    public static String MOD_ID = "gca";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static final HashMap<Player, FakePlayerInventoryContainer> fakePlayerInventoryContainerMap = new HashMap<>();

    static {
        CarpetServer.manageExtension(new GcaExtension());
    }

    @Override
    public void onPlayerLoggedIn(ServerPlayer player) {
        GcaExtension.fakePlayerInventoryContainerMap.put(player, new FakePlayerInventoryContainer(player));
    }

    @Override
    public void onPlayerLoggedOut(ServerPlayer player) {
        GcaExtension.fakePlayerInventoryContainerMap.remove(player);
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(GcaSetting.class);
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
        if (GcaSetting.fakePlayerResident) {
            JsonObject fakePlayerList = new JsonObject();
            fakePlayerInventoryContainerMap.forEach((player, fakePlayerInventoryContainer) -> {
                if (!(player instanceof EntityPlayerMPFake)) return;
                String username = player.getName().getString();
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
                fakePlayerList.add(username, fakePlayer);
            });
            File file = server.getWorldPath(LevelResource.ROOT).resolve("fake_player.gca.json").toFile();
            if (!file.isFile()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try (BufferedWriter bfw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                bfw.write(new Gson().toJson(fakePlayerList));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onServerStart(MinecraftServer server) {
        if (GcaSetting.fakePlayerResident) {
            JsonObject fakePlayerList = new JsonObject();
            File file = server.getWorldPath(LevelResource.ROOT).resolve("fake_player.gca.json").toFile();
            if (!file.isFile()) {
                return;
            }
            try (BufferedReader bfr = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                fakePlayerList = new Gson().fromJson(bfr, JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Map.Entry<String, JsonElement> entry : fakePlayerList.entrySet()) {
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
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return ComponentTranslate.getTranslations(lang);
    }

    @Override
    public void onInitialize() {

    }
}
