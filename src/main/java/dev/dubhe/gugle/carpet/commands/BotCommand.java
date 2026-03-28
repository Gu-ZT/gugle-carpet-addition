package dev.dubhe.gugle.carpet.commands;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.BotGroupInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.entry.BotRecordActionInfo;
import dev.dubhe.gugle.carpet.entry.PageInfo;
import dev.dubhe.gugle.carpet.util.BotUtil;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.util.IdUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BotCommand {
    private static final GcaConfig<BotInfo> BOT_CONFIG = GcaConfig.create("bot", BotInfo.CODEC);
    private static final GcaConfig<BotGroupInfo> BOT_GROUP_CONFIG = GcaConfig.create("bot_group", BotGroupInfo.CODEC);
    private static final GcaConfig<BotRecordActionInfo> BOT_ACTION_CONFIG = GcaConfig.create("bot_action", BotRecordActionInfo.CODEC);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(ModCommands.root(dispatcher, "bot")
            .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandBot))
            .executes(BotCommand::list)
            .then(literal("list")
                .executes(BotCommand::list)
                .then(argument("page", IntegerArgumentType.integer(1))
                    .executes(BotCommand::list)
                )
            )
            .then(literal("add")
                .then(argument("player", EntityArgument.player())
                    .then(argument("desc", StringArgumentType.greedyString())
                        .executes(BotCommand::add)
                    )
                )
            )
            .then(literal("load")
                .then(argument("player", StringArgumentType.string())
                    .suggests(BotCommand::suggestPlayer)
                    .executes(BotCommand::load)
                )
            )
            .then(literal("remove")
                .then(argument("player", StringArgumentType.string())
                    .suggests(BotCommand::suggestPlayer)
                    .executes(BotCommand::remove)
                )
            )
            .then(literal("group")
                .executes(BotCommand::groupList)
                .then(literal("create")
                    .then(argument("name", StringArgumentType.greedyString())
                        .executes(BotCommand::groupCreate)
                    )
                )
                .then(literal("generated")
                    .then(argument("name", StringArgumentType.word())
                        .executes(BotCommand::groupGenerated)
                        .then(argument("count", IntegerArgumentType.integer(1, 32))
                            .executes(BotCommand::groupGenerated)
                            .then(argument("load", BoolArgumentType.bool())
                                .executes(BotCommand::groupGenerated)
                            )
                        )
                    )
                )
                .then(literal("list")
                    .executes(BotCommand::groupList)
                    .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(BotCommand::groupList)
                    )
                )
                .then(literal("remove")
                    .then(argument("name", StringArgumentType.greedyString())
                        .executes(BotCommand::groupRemove)
                    )
                )
                .then(literal("add")
                    .then(argument("bot", StringArgumentType.string())
                        .suggests(BotCommand::suggestPlayer)
                        .then(argument("group", StringArgumentType.greedyString())
                            .suggests(BotCommand::suggestGroup)
                            .executes(BotCommand::groupAddBot)
                        )
                    )
                )
                .then(literal("remove")
                    .then(argument("bot", StringArgumentType.string())
                        .suggests(BotCommand::suggestPlayer)
                        .then(argument("group", StringArgumentType.greedyString())
                            .suggests(BotCommand::suggestGroup)
                            .executes(BotCommand::groupRemoveBot)
                        )
                    )
                )
                .then(literal("load")
                    .then(argument("group", StringArgumentType.greedyString())
                        .suggests(BotCommand::suggestGroup)
                        .executes(BotCommand::groupLoadBot)
                    )
                )
                .then(literal("unload")
                    .then(argument("group", StringArgumentType.greedyString())
                        .suggests(BotCommand::suggestGroup)
                        .executes(BotCommand::groupUnloadBot)
                    )
                )
                .then(literal("info")
                    .then(argument("group", StringArgumentType.greedyString())
                        .suggests(BotCommand::suggestGroup)
                        .executes(BotCommand::groupInfo)
                    )
                )
            )
            .then(literal("action")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandBotAction))
                .then(argument("player", StringArgumentType.string())
                    .suggests(BotCommand::suggestPlayer)
                    .executes(BotCommand::botActionList)
                    .then(literal("list")
                        .executes(BotCommand::botActionList)
                        .then(argument("page", IntegerArgumentType.integer(1))
                            .executes(BotCommand::botActionList)
                        )
                    )
                    .then(literal("create")
                        .then(argument("desc", StringArgumentType.string())
                            .then(argument("action", StringArgumentType.greedyString())
                                .executes(BotCommand::botActionAdd)
                            )
                        )
                    )
                    .then(literal("remove")
                        .then(argument("id", LongArgumentType.longArg()))
                        .executes(BotCommand::botActionRemove)
                    )
                )
            )
        );
    }

    private static void tryInit(CommandContext<CommandSourceStack> context) {
        BOT_CONFIG.tryInit(context);
        BOT_GROUP_CONFIG.tryInit(context);
        BOT_ACTION_CONFIG.tryInit(context);
    }

    @Nullable
    private static GroupNode getGroupNode(CommandContext<CommandSourceStack> context, String groupName) {
        tryInit(context);
        Map<String, BotInfo> botContents = BOT_CONFIG.getContents();
        BotGroupInfo groupInfo = BOT_GROUP_CONFIG.getContents().get(groupName);
        if (groupInfo == null) {
            context.getSource().sendFailure(Component.literal("Group %s is not found.".formatted(groupName)));
            return null;
        }
        List<BotInfo> bots = groupInfo.bots().stream()
            .map(botContents::get)
            .filter(Objects::nonNull)
            .toList();
        if (bots.size() != groupInfo.bots().size()) {
            BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, bots.stream().map(BotInfo::name).toList()));
        }
        return new GroupNode(groupInfo, bots);
    }

    private static int groupInfo(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        PageInfo<BotInfo> page = PageInfo.of(context, group.bots);
        if (page == null) return 0;
        page.pageComponents("Bot Group " + groupName, "/bot group show", BotCommand::botToComponent)
            .forEach(context.getSource()::sendSystemMessage);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupUnloadBot(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        PlayerList players = context.getSource().getServer().getPlayerList();
        for (BotInfo bot : group.bots) {
            ServerPlayer player = players.getPlayerByName(bot.name());
            if (!(player instanceof EntityPlayerMPFake fake)) continue;
            fake.kill(Component.literal("Killed"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int groupLoadBot(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        CommandSourceStack source = context.getSource();
        return (int) group.bots.stream().filter(it -> spawnBot(source, it)).count();
    }

    private static int groupRemoveBot(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        String botName = StringArgumentType.getString(context, "bot");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        CommandSourceStack source = context.getSource();
        List<String> bots = new ArrayList<>(group.group.bots());
        if (!bots.remove(botName)) {
            source.sendFailure(Component.literal("Bot %s is not found in the %s.".formatted(botName, groupName)));
            return 0;
        }
        BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, bots));
        source.sendSuccess(() -> Component.literal("Bot %s is removed from %s successfully.".formatted(botName, groupName)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupAddBot(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        String botName = StringArgumentType.getString(context, "bot");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        CommandSourceStack source = context.getSource();
        List<String> bots = new ArrayList<>(group.group.bots());
        if (bots.contains(botName)) {
            source.sendFailure(Component.literal("Bot %s is already added.".formatted(botName)));
            return 0;
        }
        bots.add(botName);
        BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, bots));
        source.sendSuccess(() -> Component.literal("Bot %s is added to %s successfully.".formatted(botName, groupName)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupGenerated(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String groupName = StringArgumentType.getString(context, "name");
        int groupCount = IntegerArgumentType.getInteger(context, "count");
        boolean groupLoad = BoolArgumentType.getBool(context, "load");
        CommandSourceStack source = context.getSource();
        if (BOT_GROUP_CONFIG.getContents().containsKey(groupName)) {
            source.sendFailure(Component.literal("Group %s already exists.".formatted(groupName)));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command source must be player."));
            return 0;
        }

        Map<String, BotInfo> botContents = BOT_CONFIG.getContents();
        List<BotInfo> bots = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            String botName = "bot_%s_%s".formatted(groupName, i);
            if (botContents.containsKey(botName)) {
                source.sendFailure(Component.literal("Bot %s already exists.".formatted(botName)));
                continue;
            }
            BotInfo bot = BotInfo.create(botName, botName, player, new EntityPlayerActionPack(player));
            BOT_CONFIG.update(bot, false);
            bots.add(bot);
        }
        BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, bots.stream().map(BotInfo::name).toList()));
        if (groupLoad) {
            for (BotInfo bot : bots) {
                spawnBot(source, bot);
            }
        }
        source.sendSuccess(() -> Component.literal("Group %s generated successfully.".formatted(groupName)), false);
        return bots.size();
    }

    private static int groupCreate(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String groupName = StringArgumentType.getString(context, "name");
        CommandSourceStack source = context.getSource();
        if (BOT_GROUP_CONFIG.getContents().containsKey(groupName)) {
            source.sendFailure(Component.literal("Group %s already exists.".formatted(groupName)));
            return 0;
        }
        BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, List.of()));
        source.sendSuccess(() -> Component.literal("Group %s created successfully.".formatted(groupName)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupRemove(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String groupName = StringArgumentType.getString(context, "name");
        if (BOT_GROUP_CONFIG.remove(groupName) == null) {
            context.getSource().sendFailure(Component.literal("Bot Group %s is not exist.".formatted(groupName)));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal("%s is removed.".formatted(groupName)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupList(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        PageInfo<BotGroupInfo> page = PageInfo.of(context, BOT_GROUP_CONFIG.getContents().values());
        if (page == null) return 0;
        page.pageComponents("Bot Group List", "/bot group list", BotCommand::botGroupToComponent)
            .forEach(context.getSource()::sendSystemMessage);
        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent botGroupToComponent(BotGroupInfo botGroupInfo) {
        MutableComponent name = Component.literal(botGroupInfo.name()).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(botGroupInfo.name())))
        );
        MutableComponent load = Component.literal("[↑]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Load Group")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot group load %s".formatted(botGroupInfo.name())
                ))
        );
        MutableComponent remove = Component.literal("[↓]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Unload Group")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot group unload %s".formatted(botGroupInfo.name())
                ))
        );
        MutableComponent info = Component.literal("[i]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Group Info")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot group info %s".formatted(botGroupInfo.name())
                ))
        );
        MutableComponent delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove Bot Group")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot group remove %s".formatted(botGroupInfo.name())
                ))
        );
        MutableComponent component = Component.literal("▶ ").append(name);
        component.append(" ").append(load);
        component.append(" ").append(remove);
        component.append(" ").append(info);
        return component.append(" ").append(delete);
    }

    private static int load(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String name = StringArgumentType.getString(context, "player");
        BotInfo bot = BOT_CONFIG.getContents().get(name);
        return spawnBot(context.getSource(), bot) ? Command.SINGLE_SUCCESS : 0;
    }

    private static boolean spawnBot(CommandSourceStack source, @Nullable BotInfo bot) {
        if (bot == null) {
            source.sendFailure(Component.literal("%s is not exist."));
            return false;
        }
        //#if MC>=12002
        if (
            //#if MC < 12104
            BotUtil.isGcaSpawningBot(bot.name())
            //#else
            //$$ EntityPlayerMPFake.isSpawningPlayer(bot.name())
            //#endif
        ) {
            source.sendFailure(Component.literal("Player %s is currently logging on".formatted(bot.name())));
            return false;
        }
        //#endif
        if (source.getServer().getPlayerList().getPlayerByName(bot.name()) != null) {
            source.sendFailure(Component.literal("player %s is already exist.".formatted(bot.name())));
            return false;
        }
        boolean success = BotUtil.spawnBot(source.getServer(), bot);
        if (!success) {
            source.sendFailure(Component.literal("%s is not loaded.".formatted(bot.name())));
        }
        return success;

    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        tryInit(context);
        CommandSourceStack source = context.getSource();
        ServerPlayer p;
        if (!((p = EntityArgument.getPlayer(context, "player")) instanceof EntityPlayerMPFake player)) {
            source.sendFailure(Component.literal("%s is not a fake player.".formatted(
                p.getGameProfile().getName()
            )));
            return 0;
        }
        String name = player.getGameProfile().getName();
        if (BOT_CONFIG.getContents().containsKey(name)) {
            source.sendFailure(Component.literal("%s is already save.".formatted(name)));
            return 0;
        }
        BOT_CONFIG.update(BotInfo.create(
            name,
            StringArgumentType.getString(context, "desc"),
            player,
            ((ServerPlayerInterface) player).getActionPack()
        ));
        source.sendSuccess(() -> Component.literal("%s is added.".formatted(name)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String name = StringArgumentType.getString(context, "player");
        if (BOT_CONFIG.remove(name) == null) {
            context.getSource().sendFailure(Component.literal("Bot %s is not exist.".formatted(name)));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal("%s is removed.".formatted(name)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        PageInfo<BotInfo> page = PageInfo.of(context, BOT_CONFIG.getContents().values());
        if (page == null) return 0;
        page.pageComponents("Bot List", "/bot list", BotCommand::botToComponent)
            .forEach(context.getSource()::sendSystemMessage);
        return Command.SINGLE_SUCCESS;
    }

    private static int botActionList(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String name = StringArgumentType.getString(context, "player");
        BotRecordActionInfo actionInfo = BOT_ACTION_CONFIG.getContents().get(name);
        var page = PageInfo.of(context, actionInfo == null ? List.of() : actionInfo.ofActions());
        if (page == null) return 0;
        page.pageComponents(
            "Bot §b%s§r's Action List".formatted(name),
            "/bot action %s list".formatted(name),
            info -> botActionToComponent(info, name)
        ).forEach(context.getSource()::sendSystemMessage);
        return Command.SINGLE_SUCCESS;
    }

    private static int botActionAdd(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String name = StringArgumentType.getString(context, "player");
        String desc = StringArgumentType.getString(context, "desc");
        String action = StringArgumentType.getString(context, "action");
        if (!BOT_CONFIG.getContents().containsKey(name)) {
            context.getSource().sendFailure(Component.literal("Bot %s is not exist.".formatted(name)));
            return 0;
        }
        BotRecordActionInfo actionInfo = BOT_ACTION_CONFIG.getContents()
            .computeIfAbsent(name, BotRecordActionInfo::create);
        List<BotRecordActionInfo.BotExecActionInfo> actions = new ArrayList<>(actionInfo.ofActions());
        actions.add(new BotRecordActionInfo.BotExecActionInfo(IdUtil.nextId(), desc, action));
        BOT_ACTION_CONFIG.update(actionInfo.ofActions(actions));
        context.getSource().sendSuccess(() -> Component.literal("Action %s is added.".formatted(desc)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int botActionRemove(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String name = StringArgumentType.getString(context, "player");
        BotRecordActionInfo actionInfo = BOT_ACTION_CONFIG.getContents().get(name);
        if (actionInfo == null) {
            context.getSource().sendFailure(Component.literal("Bot %s is not exist.".formatted(name)));
            return 0;
        }
        long id = LongArgumentType.getLong(context, "id");
        List<BotRecordActionInfo.BotExecActionInfo> actions = new ArrayList<>(actionInfo.ofActions());
        if (!actions.removeIf(it -> it.id() == id)) {
            context.getSource().sendFailure(Component.literal("Action %s is not exist.".formatted(id)));
            return 0;
        }
        BOT_ACTION_CONFIG.update(actionInfo.ofActions(actions));
        context.getSource().sendSuccess(() -> Component.literal("Action %s is removed.".formatted(id)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent botToComponent(BotInfo botInfo) {
        MutableComponent desc = Component.literal(botInfo.desc()).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(botInfo.name())))
        );
        boolean notOnline = BOT_CONFIG.getServer().getPlayerList().getPlayerByName(botInfo.name()) == null;
        MutableComponent load = Component.literal("[↑]").withStyle(
            Style.EMPTY
                .applyFormat(notOnline ? ChatFormatting.GREEN : ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Load bot")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/bot load %s".formatted(botInfo.name())))
        );
        MutableComponent remove = Component.literal("[↓]").withStyle(
            Style.EMPTY
                .applyFormat(notOnline ? ChatFormatting.GRAY : ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Unload bot")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/player %s kill".formatted(botInfo.name())))
        );
        MutableComponent delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove bot")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot remove %s".formatted(botInfo.name())
                ))
        );
        MutableComponent component = Component.literal("▶ ")
            .withStyle(notOnline ? ChatFormatting.RED : ChatFormatting.GREEN)
            .append(desc);
        component.append(" ").append(load);
        component.append(" ").append(remove);
        return component.append(" ").append(delete);
    }

    private static MutableComponent botActionToComponent(BotRecordActionInfo.BotExecActionInfo info, String name) {
        String command = "/player %s %s".formatted(name, info.action());
        MutableComponent desc = Component.literal(info.desc()).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(command)))
        );
        MutableComponent exec = Component.literal("[▶]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Execution Action")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, command))
        );

        MutableComponent delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove Action")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot action %s remove %s".formatted(name, info.id())
                ))
        );
        return Component.literal("▶ ")
            .append(desc).append(" ")
            .append(exec).append(" ")
            .append(delete);
    }

    private static CompletableFuture<Suggestions> suggestPlayer(
        final CommandContext<CommandSourceStack> context,
        final SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(BOT_CONFIG.getContents().keySet(), builder);
    }

    private static CompletableFuture<Suggestions> suggestGroup(
        final CommandContext<CommandSourceStack> context,
        final SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(BOT_GROUP_CONFIG.getContents().keySet(), builder);
    }

    record GroupNode(BotGroupInfo group, List<BotInfo> bots) {
    }
}
