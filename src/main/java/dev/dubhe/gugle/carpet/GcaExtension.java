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
import dev.dubhe.gugle.carpet.commands.BlistCommand;
import dev.dubhe.gugle.carpet.commands.BotCommand;
import dev.dubhe.gugle.carpet.commands.HereCommand;
import dev.dubhe.gugle.carpet.commands.LocCommand;
import dev.dubhe.gugle.carpet.commands.SopCommand;
import dev.dubhe.gugle.carpet.commands.TodoCommand;
import dev.dubhe.gugle.carpet.commands.WhereisCommand;
import dev.dubhe.gugle.carpet.commands.WlistCommand;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.tools.GameProfileHelper;
import dev.dubhe.gugle.carpet.tools.ResourceLocationSerializer;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
//#if MC>=12106
//$$ import net.minecraft.util.ProblemReporter;
//$$ import net.minecraft.world.level.storage.TagValueOutput;
//#endif
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class GcaExtension implements CarpetExtension, ModInitializer {
    private static final HashSet<EntityPlayerMPFake> RESIDENT_PLAYERS = new HashSet<>();
    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeHierarchyAdapter(ResourceKey.class, new DimTypeSerializer())
        .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocationSerializer())
        .registerTypeHierarchyAdapter(ChatFormatting.class, new ChatFormattingSerializer())
        .registerTypeHierarchyAdapter(WelcomeMessage.MessageData.class, new WelcomeMessage.MessageData.Serializer())
        .create();
    public static String MOD_ID = "gca";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final HashMap<String, Consumer<ServerPlayer>> ON_PLAYER_LOGGED_IN = new HashMap<>();
    public static final List<Map.Entry<Long, Runnable>> PLAN_FUNCTION = new ArrayList<>();

    public static ResourceLocation id(String path) {
        //#if MC>=12100
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
        //#else
        //$$ return new ResourceLocation(MOD_ID, path);
        //#endif
    }

    @Override
    public void onPlayerLoggedIn(ServerPlayer player) {
        GameProfileHelper.prasePlayerGameProfile(
            player, (profile, name, uuid) -> {
                Level level = player.level();
                MinecraftServer server = level instanceof ServerLevel serverLevel ? serverLevel.getServer() : null;
                if (server != null && server.isSingleplayer() && server.isSingleplayerOwner(profile)) {
                    loadSavedPlayer(server);
                }
                Consumer<ServerPlayer> consumer = ON_PLAYER_LOGGED_IN.remove(name);
                if (consumer != null) consumer.accept(player);
                if (GcaSetting.welcomePlayer) WelcomeMessage.onPlayerLoggedIn(player);
                if (player instanceof EntityPlayerMPFake fakePlayer) {
                    RESIDENT_PLAYERS.add(fakePlayer);
                }
            }
        );
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
        GcaConfig.CONFIGS.values().forEach(it -> it.tryInit(server));

        BlistCommand.PERMISSION.init(server);
        BotCommand.BOT_CONFIG.tryInit(server);
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
                    CompoundTag tag;
                    //#if MC>=12106
                    //$$ try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(player.problemPath(), GcaExtension.LOGGER)) {
                    //$$     TagValueOutput valueOutput = TagValueOutput.createWithContext(reporter, player.registryAccess());
                    //$$     player.saveWithoutId(valueOutput);
                    //$$     tag = valueOutput.buildResult();
                    //$$ }
                    //#else
                    tag = player.saveWithoutId(new CompoundTag());
                    //#endif
                    if (tag.contains("gca.NoResident")) {
                        continue;
                    }
                    GameProfileHelper.prasePlayerGameProfile(
                        player,
                        (profile, name, uuid) -> fakePlayerList.add(name, FakePlayerResident.save(player))
                    );
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
        if (server.isSingleplayer()) return;
        loadSavedPlayer(server);
    }

    public void loadSavedPlayer(MinecraftServer server) {
        if (GcaSetting.fakePlayerResident) {
            File oldFile = server.getWorldPath(LevelResource.ROOT).resolve("fake_player.gca.old.json").toFile();
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
            if (oldFile.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                oldFile.delete();
            }
            //noinspection ResultOfMethodCallIgnored
            file.renameTo(oldFile);
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
    public @Nullable Map<String, String> canHasTranslations(String lang) {
        return ComponentTranslate.getTranslations(lang);
    }

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(this);
    }

    public static ResourceLocation parseLocation(String string) {
        //#if MC>=12100
        return ResourceLocation.parse(string);
        //#else
        //$$ return new ResourceLocation(string);
        //#endif
    }
}
