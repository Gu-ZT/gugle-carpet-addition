package dev.dubhe.gugle.carpet.util;

import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import net.minecraft.server.MinecraftServer;

public class BotUtil {
    public static boolean spawnBot(MinecraftServer server, BotInfo bot) {
        EntityPlayerMPFake instance = EntityPlayerMPFake.createFake(
            bot.name(),
            server,
            bot.pos(),
            bot.facing().y,
            bot.facing().x,
            bot.dimension(),
            bot.mode(),
            bot.flying()
        );

        if (instance != null) {
            bot.actions().applyAction(instance);
            return true;
        } else {
            return false;
        }
    }

}
