package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.tools.PosUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class WhereisCommand {
    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "whereis")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandWhereis))
                .then(
                    Commands.argument("player", EntityArgument.player())
                        .executes(WhereisCommand::execute)
                )
        );
        dispatcher.register(
            ModCommands.root(dispatcher, "vris")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandWhereis))
                .then(
                    Commands.argument("player", EntityArgument.player())
                        .executes(WhereisCommand::execute)
                )
        );
    }

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        for (MutableComponent component : PosUtils.playerPos(player)) {
            context.getSource().sendSuccess(() -> component, false);
        }
        return 1;
    }
}
