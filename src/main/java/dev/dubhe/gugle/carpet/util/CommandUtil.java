package dev.dubhe.gugle.carpet.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.dubhe.gugle.carpet.entry.PlayerGameProfileInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;

import java.util.List;

public class CommandUtil {

    public static List<PlayerGameProfileInfo> parseGameProfiles(
        CommandContext<CommandSourceStack> context,
        String name
    ) throws CommandSyntaxException {
        return GameProfileArgument.getGameProfiles(context, name)
            .stream()
            .map(PlayerGameProfileInfo::of)
            .toList();
    }

}
