package dev.dubhe.gugle.carpet.tools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;


public class ModCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> root(CommandDispatcher<CommandSourceStack> dispatcher, String string) {
        String prefix = "";
        if (dispatcher.getRoot().getChild(string) != null) prefix = "gca";
        return Commands.literal(prefix + string);
    }
}
