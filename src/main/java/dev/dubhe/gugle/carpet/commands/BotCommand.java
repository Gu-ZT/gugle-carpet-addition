package dev.dubhe.gugle.carpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class BotCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bot")
                .then(Commands.literal("list"))
                .then(Commands.literal("add"))
                .then(Commands.literal("remove"))
        );
    }
}
