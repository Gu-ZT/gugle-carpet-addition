package dev.dubhe.gugle.carpet;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.patches.EntityPlayerMPFake;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.commands.*;
import dev.dubhe.gugle.carpet.tools.WelcomeMessage;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerResident;
import dev.dubhe.gugle.carpet.tools.serializer.ChatFormattingSerializer;
import dev.dubhe.gugle.carpet.tools.serializer.DimTypeSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class GcaExtension implements CarpetExtension, ModInitializer {
    private static final HashSet<EntityPlayerMPFake> RESIDENT_PLAYERS = new HashSet<>();
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(ResourceKey.class, new DimTypeSerializer())
            .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeHierarchyAdapter(ChatFormatting.class, new ChatFormattingSerializer())
            .registerTypeHierarchyAdapter(WelcomeMessage.MessageData.class, new WelcomeMessage.MessageData.Serializer())
            .create();
    public static String MOD_ID = "gca";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final HashMap<String, Consumer<ServerPlayer>> ON_PLAYER_LOGGED_IN = new HashMap<>();
    public static final List<Map.Entry<Long, Runnable>> PLAN_FUNCTION = new ArrayList<>();

    public static @NotNull ResourceLocation id(String path) {
        //#if MC>=12100
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
        //#else
        //$$ return new ResourceLocation(MOD_ID, path);
        //#endif
    }

    @Override
    public void onPlayerLoggedIn(@NotNull ServerPlayer player) {
        Consumer<ServerPlayer> consumer = ON_PLAYER_LOGGED_IN.remove(player.getGameProfile().getName());
        if (consumer != null) consumer.accept(player);
        if (GcaSetting.welcomePlayer) WelcomeMessage.onPlayerLoggedIn(player);
        if (player instanceof EntityPlayerMPFake fakePlayer) {
            RESIDENT_PLAYERS.add(fakePlayer);
        }
    }

    @Override
    public void onPlayerLoggedOut(ServerPlayer player) {
        if (player instanceof EntityPlayerMPFake) {
            RESIDENT_PLAYERS.remove(player);
        }
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(GcaSetting.class);
    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        BlistCommand.PERMISSION.init(server);
        BotCommand.BOT_INFO.init(server);
        LocCommand.LOC_POINT.init(server);
        TodoCommand.TODO.init(server);
        WlistCommand.PERMISSION.init(server);
        WelcomeMessage.WELCOME_MESSAGE.init(server);
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
        try {
            if (GcaSetting.fakePlayerResident) {
                JsonObject fakePlayerList = new JsonObject();
                for (EntityPlayerMPFake player : RESIDENT_PLAYERS) {
                    if (player.saveWithoutId(new CompoundTag()).contains("gca.NoResident")) {
                        continue;
                    }
                    String username = player.getGameProfile().getName();
                    fakePlayerList.add(username, FakePlayerResident.save(player));
                }
                File file = server.getWorldPath(LevelResource.ROOT).resolve("fake_player.gca.json").toFile();
                // 文件不需要存在
                try (BufferedWriter bfw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                    bfw.write(GSON.toJson(fakePlayerList));
                } catch (Exception e) {
                    GcaExtension.LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            RESIDENT_PLAYERS.clear();
        }
    }

    @Override
    public void onServerLoadedWorlds(MinecraftServer server) {
        if (GcaSetting.fakePlayerResident) {
            File file = server.getWorldPath(LevelResource.ROOT).resolve("fake_player.gca.json").toFile();
            if (!file.isFile()) {
                return;
            }
            try (BufferedReader bfr = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                JsonObject fakePlayerList = GSON.fromJson(bfr, JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : fakePlayerList.entrySet()) {
                    FakePlayerResident.load(entry, server);
                }
            } catch (IOException e) {
                GcaExtension.LOGGER.error(e.getMessage(), e);
            }
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        BotCommand.register(dispatcher);
        LocCommand.register(dispatcher);
        HereCommand.register(dispatcher);
        WhereisCommand.register(dispatcher);
        TodoCommand.register(dispatcher);
        WlistCommand.register(dispatcher);
        BlistCommand.register(dispatcher);
        SopCommand.register(dispatcher);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return ComponentTranslate.getTranslations(lang);
    }

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(this);
    }

    public static @NotNull ResourceLocation parseLocation(String string) {
        //#if MC>=12100
        return ResourceLocation.parse(string);
        //#else
        //$$ return new ResourceLocation(string);
        //#endif
    }
}
