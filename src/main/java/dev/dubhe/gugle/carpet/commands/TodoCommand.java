package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.PageInfo;
import dev.dubhe.gugle.carpet.entry.TodoInfo;
import dev.dubhe.gugle.carpet.util.IdUtil;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TodoCommand {
    private static final GcaConfig<TodoInfo> TODO_CONFIG = GcaConfig.create("todo", TodoInfo.CODEC);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "todo")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandTodo))
                .executes(TodoCommand::list)
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("desc", StringArgumentType.greedyString())
                                .executes(TodoCommand::add)
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("id", LongArgumentType.longArg())
                                .suggests(TODO_CONFIG::suggestKeys)
                                .executes(TodoCommand::remove)
                        )
                )
                .then(
                    Commands.literal("list")
                        .executes(TodoCommand::list)
                        .then(
                            Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(TodoCommand::list)
                        )
                )
                .then(
                    Commands.literal("success")
                        .then(
                            Commands.argument("id", LongArgumentType.longArg())
                                .suggests(TODO_CONFIG::suggestKeys)
                                .executes(TodoCommand::success)
                                .then(
                                    Commands.argument("success", BoolArgumentType.bool())
                                        .executes(TodoCommand::success)
                                )
                        )
                )
        );
    }

    public static int add(CommandContext<CommandSourceStack> context) {
        TODO_CONFIG.tryInit(context);
        CommandSourceStack source = context.getSource();
        long id = IdUtil.nextId();
        String desc = StringArgumentType.getString(context, "desc");
        TODO_CONFIG.update(new TodoInfo(id, desc, false));
        source.sendSuccess(() -> Component.literal("Todo %s is added.".formatted(desc)), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int remove(CommandContext<CommandSourceStack> context) {
        TODO_CONFIG.tryInit(context);
        long id = LongArgumentType.getLong(context, "id");
        TodoInfo removed = TODO_CONFIG.remove(String.valueOf(id));
        if (removed == null) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("No such todo id %s", id));
            return 0;
        }
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("Todo %s is removed.", removed.desc()), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int success(CommandContext<CommandSourceStack> context) {
        TODO_CONFIG.tryInit(context);
        long id = LongArgumentType.getLong(context, "id");
        boolean success = getSuccess(context);
        TodoInfo todo = TODO_CONFIG.get(String.valueOf(id));
        if (todo == null) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("No such todo id %s", id));
            return 0;
        }
        TODO_CONFIG.update(todo.ofSuccess(success));
        if (success) {
            Component name = context.getSource().getDisplayName();
            context.getSource().getServer().getPlayerList().broadcastSystemMessage(
                ComponentHelper.fmtHlt("%s has completed Todo %s.", name, todo.desc()),
                false
            );
        } else {
            context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("Set Todo %s as incomplete.", todo.desc()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static boolean getSuccess(CommandContext<CommandSourceStack> context) {
        try {
            return BoolArgumentType.getBool(context, "success");
        } catch (IllegalArgumentException ignored) {
            return true;
        }
    }

    public static int list(CommandContext<CommandSourceStack> context) {
        TODO_CONFIG.tryInit(context);
        PageInfo<TodoInfo> page = PageInfo.of(context, TODO_CONFIG.values());
        if (page == null) return 0;
        page.sendPageInfo(context, "msg.gca.todo.list", "/todo list");
        return Command.SINGLE_SUCCESS;
    }
}
