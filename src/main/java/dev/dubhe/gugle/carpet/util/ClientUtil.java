package dev.dubhe.gugle.carpet.util;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.world.entity.player.Player;

public class ClientUtil {
    public static boolean isFakePlayer(Player player) {
        if (player.level().isClientSide()) {
            return player.getScore() == -114514;
        }
        return player instanceof EntityPlayerMPFake;
    }
}
