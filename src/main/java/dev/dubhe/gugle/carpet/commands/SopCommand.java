package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.tools.GameProfileHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class SopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "sop")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandSop))
                .executes(SopCommand::sop)
        );
    }

    public static int sop(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) return 0;
        PlayerList playerList = source.getServer().getPlayerList();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        GameProfileHelper.prasePlayerGameProfile(
            player, (profile, name, uuid) -> {
                if (!playerList.isOp(profile)) {
                    playerList.op(profile);
                    source.sendSuccess(() -> Component.translatable("commands.op.success", name), true);
                }
            }
        );
        return Command.SINGLE_SUCCESS;
    }
}
