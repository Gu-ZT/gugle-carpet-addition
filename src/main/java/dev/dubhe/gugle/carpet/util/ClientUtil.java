package dev.dubhe.gugle.carpet.util;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import javax.annotation.Nullable;

public class ClientUtil {
    private static @Nullable PlayerInfo getPlayerInfo(UUID uuid) {
        if (Minecraft.getInstance().getConnection() != null) {
            return Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
        }
        return null;
    }

    public static boolean isFakePlayer(Player player) {
        if (player.level().isClientSide()) {
            PlayerInfo info = getPlayerInfo(player.getUUID());
            return info != null && info.getLatency() == 0;
        }
        return player instanceof EntityPlayerMPFake;
    }
}
