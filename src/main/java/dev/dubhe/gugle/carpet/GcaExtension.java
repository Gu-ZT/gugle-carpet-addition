package dev.dubhe.gugle.carpet;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.mojang.brigadier.CommandDispatcher;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import dev.dubhe.gugle.carpet.commands.BlistCommand;
import dev.dubhe.gugle.carpet.commands.BotCommand;
import dev.dubhe.gugle.carpet.commands.HereCommand;
import dev.dubhe.gugle.carpet.commands.LocCommand;
import dev.dubhe.gugle.carpet.commands.SopCommand;
import dev.dubhe.gugle.carpet.commands.TodoCommand;
import dev.dubhe.gugle.carpet.commands.WhereisCommand;
import dev.dubhe.gugle.carpet.commands.WlistCommand;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.PlayerGameProfileCache;
import dev.dubhe.gugle.carpet.tools.WelcomeMessage;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GcaExtension implements CarpetExtension, ModInitializer {
    public static final String MOD_ID = "gca";
    public static final String MOD_NAME = "GugleCarpetAddition";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final HashMap<String, Consumer<ServerPlayer>> ON_PLAYER_LOGGED_IN = new HashMap<>();
    public static final List<Map.Entry<Long, Runnable>> PLAN_FUNCTION = new ArrayList<>();

    public static ResourceLocation id(String path) {
        //#if MC>=12100
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
        //#else
        //$$ return new ResourceLocation(MOD_ID, path);
        //#endif
    }

    public static ResourceLocation parseLocation(String string) {
        //#if MC>=12100
        return ResourceLocation.parse(string);
        //#else
        //$$ return new ResourceLocation(string);
        //#endif
    }

    @Override
    public void onPlayerLoggedIn(ServerPlayer player) {
        PlayerGameProfileCache info = PlayerGameProfileCache.of(player);
        Consumer<ServerPlayer> consumer = ON_PLAYER_LOGGED_IN.remove(info.name());
        if (consumer != null) consumer.accept(player);
        if (GcaSetting.welcomePlayer) WelcomeMessage.onPlayerLoggedIn(player);
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(GcaSetting.class);
    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        GcaConfig.CONFIGS.values().forEach(it -> it.tryInit(server));
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
        return ComponentHelper.fetchLanguage(lang);
    }

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(this);
        WelcomeMessage.registerDefaultReplacer();
    }

}
