package dev.dubhe.gugle.carpet.util;

import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.NameBooleanInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class PermissionUtil {

    public static boolean hasPermission(GcaConfig<NameBooleanInfo> permission, CommandSourceStack stack) {
        if (
            //#if MC >= 12111
            //$$ Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(stack)
            //#else
            stack.hasPermission(Commands.LEVEL_GAMEMASTERS)
            //#endif
        ) return true;
        if (!stack.isPlayer()) return true;
        ServerPlayer player = stack.getPlayer();
        if (player == null) return false;
        String uuid = player.getGameProfile().getId().toString();
        return Optional.ofNullable(permission.getContents().get(uuid)).map(NameBooleanInfo::status).orElse(false);
    }

}
