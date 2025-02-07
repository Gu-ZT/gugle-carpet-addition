package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.GcaValidators;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.tools.PosUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class HereCommand {
    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        if (GcaValidators.CARPET_AMS_ADDITION) {
            return;
        }
        dispatcher.register(
            ModCommands.root(dispatcher, "here")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandHere))
                .executes(HereCommand::execute)
        );
    }

    public static int execute(@NotNull CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) return 0;
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        for (MutableComponent component : PosUtils.playerPos(player)) {
            server.getPlayerList().broadcastSystemMessage(component, false);
        }
        return 1;
    }
}
