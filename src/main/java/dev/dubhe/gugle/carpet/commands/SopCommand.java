package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.NotNull;

public class SopCommand {
    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "sop")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandSop))
                .executes(SopCommand::sop)
        );
    }

    public static int sop(@NotNull CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) return 0;
        PlayerList playerList = source.getServer().getPlayerList();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        GameProfile gameProfile = player.getGameProfile();
        if (!playerList.isOp(gameProfile)) {
            playerList.op(gameProfile);
            source.sendSuccess(() -> Component.translatable("commands.op.success", gameProfile.getName()), true);
        }
        return 1;
    }
}
