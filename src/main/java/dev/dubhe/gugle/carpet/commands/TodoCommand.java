package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.PageInfo;
import dev.dubhe.gugle.carpet.entry.TodoInfo;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import dev.dubhe.gugle.carpet.util.IdUtil;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.concurrent.CompletableFuture;

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
                                .suggests(TodoCommand::suggestId)
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
                                .suggests(TodoCommand::suggestId)
                                .executes(TodoCommand::success)
                                .then(
                                    Commands.argument("success", BoolArgumentType.bool())
                                        .executes(TodoCommand::success)
                                )
                        )
                )
        );
    }

    private static CompletableFuture<Suggestions> suggestId(
        final CommandContext<CommandSourceStack> context,
        final SuggestionsBuilder builder
    ) {
        TODO_CONFIG.tryInit(context);
        return SharedSuggestionProvider.suggest(TODO_CONFIG.getContents().keySet(), builder);
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
            context.getSource().sendFailure(Component.literal("No such todo id %s".formatted(id)));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal("Todo %s is removed.".formatted(removed.desc())), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int success(CommandContext<CommandSourceStack> context) {
        TODO_CONFIG.tryInit(context);
        long id = LongArgumentType.getLong(context, "id");
        boolean success = getSuccess(context);
        TodoInfo todo = TODO_CONFIG.getContents().get(String.valueOf(id));
        if (todo == null) {
            context.getSource().sendFailure(Component.literal("No such todo id %s".formatted(id)));
            return 0;
        }
        TODO_CONFIG.update(todo.ofSuccess(success));
        context.getSource()
            .sendSuccess(() -> Component.literal("Todo %s has be %s.".formatted(todo.desc(), success ? "done" : "undone")), false);
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
        PageInfo<TodoInfo> page = PageInfo.of(context, TODO_CONFIG.getContents().values());
        if (page == null) return 0;
        page.pageComponents("Todo List", "/todo list", TodoCommand::TodoToComponent)
            .forEach(context.getSource()::sendSystemMessage);
        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent TodoToComponent(TodoInfo todo) {
        MutableComponent component = Component.literal(todo.desc()).withStyle(
            Style.EMPTY
                .withStrikethrough(todo.success())
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(todo.name())))
        );
        MutableComponent success = Component.literal("[✔]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Make todo done")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/todo success %s".formatted(todo.id())))
        );
        MutableComponent unSuccess = Component.literal("[❌]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Make todo undone")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/todo success %s false".formatted(todo.id())))
        );
        MutableComponent remove = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove todo")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/todo remove %s".formatted(todo.id())))
        );
        return Component.literal(todo.success() ? "☑" : "☐")
            .append(" ").append(component)
            .append(" ").append(todo.success() ? unSuccess : success)
            .append(" ").append(remove);
    }
}
