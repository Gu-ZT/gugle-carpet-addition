package dev.dubhe.gugle.carpet.util;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public class CommandUtil {
    public static int getPage(CommandContext<CommandSourceStack> context) {
        try {
            return IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {
            return 1;
        }
    }

}
