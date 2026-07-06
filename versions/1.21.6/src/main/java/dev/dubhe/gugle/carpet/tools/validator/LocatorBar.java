package dev.dubhe.gugle.carpet.tools.validator;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import carpet.patches.EntityPlayerMPFake;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.waypoints.ServerWaypointManager;

import javax.annotation.Nullable;

public class LocatorBar extends Validator<Boolean> {
    @Override
    public Boolean validate(@Nullable CommandSourceStack source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
        if (source == null) return newValue;
        MinecraftServer server = source.getServer();
        PlayerList list = server.getPlayerList();
        if (!newValue) {
            for (ServerPlayer player : list.getPlayers()) {
                if (!(player instanceof EntityPlayerMPFake)) continue;
                ServerWaypointManager manager = player.level().getWaypointManager();
                manager.removePlayer(player);
            }
        } else {
            for (ServerPlayer player : list.getPlayers()) {
                if (!(player instanceof EntityPlayerMPFake)) continue;
                ServerWaypointManager manager = player.level().getWaypointManager();
                manager.addPlayer(player);
            }
        }
        return newValue;
    }
}
