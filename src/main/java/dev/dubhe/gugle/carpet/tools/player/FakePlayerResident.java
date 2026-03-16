package dev.dubhe.gugle.carpet.tools.player;

import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.util.BotUtil;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class FakePlayerResident {
    private final GcaConfig<BotInfo> config;
    private final MinecraftServer server;

    public FakePlayerResident(MinecraftServer server) {
        this.config = GcaConfig.create("residents", BotInfo.CODEC, false);
        this.server = server;
        this.config.tryInit(server);
    }

    public void save() {
        GcaExtension.LOGGER.info("Saving fake player resident...");
        if (!GcaSetting.fakePlayerResident) return;

        List<BotInfo> bots = this.server.getPlayerList()
            .getPlayers()
            .stream()
            .filter(it -> it instanceof EntityPlayerMPFake)
            .map(it -> BotInfo.create(it, "Resident bot", GcaSetting.fakePlayerReloadAction))
            .toList();
        this.config.set(bots);
    }

    public void load() {
        if (!GcaSetting.fakePlayerResident) return;
        List<BotInfo> bots = this.config.getContents().values().stream().toList();
        for (BotInfo bot : bots) {
            //#if MC>=12100
            if (
                //#if MC < 12104
                BotUtil.isGcaSpawningBot(bot.name())
                //#else
                //$$ EntityPlayerMPFake.isSpawningPlayer(bot.name())
                //#endif
            ) {
                GcaExtension.LOGGER.warn("Player {} is currently logging on", bot.name());
                continue;
            }
            //#endif
            if (this.server.getPlayerList().getPlayerByName(bot.name()) != null) {
                GcaExtension.LOGGER.warn("player {} is already exist.", bot.name());
                continue;
            }
            if (!BotUtil.spawnBot(this.server, bot, GcaSetting.fakePlayerReloadAction)) {
                GcaExtension.LOGGER.warn("{} is not loaded.", bot.name());
            }
        }
    }
}
