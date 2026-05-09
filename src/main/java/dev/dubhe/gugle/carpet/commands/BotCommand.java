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
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.BotExecutorInfo;
import dev.dubhe.gugle.carpet.entry.BotGroupInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.entry.PageInfo;
import dev.dubhe.gugle.carpet.util.BotUtil;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.util.CommandUtil;
import dev.dubhe.gugle.carpet.util.IdUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BotCommand {
    private static final Pattern ACTION_PATTERN = Pattern.compile("bot action (\\S+) add (\"[^\"]+\"|\\S+)(?:\\s+(.*))?");
    private static final GcaConfig<BotInfo> BOT_CONFIG = GcaConfig.create("bot", BotInfo.CODEC);
    private static final GcaConfig<BotGroupInfo> BOT_GROUP_CONFIG = GcaConfig.create("bot_group", BotGroupInfo.CODEC);

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
                    .suggests(BOT_CONFIG::suggestKeys)
                    .executes(BotCommand::load)
                )
            )
            .then(literal("remove")
                .then(argument("player", StringArgumentType.string())
                    .suggests(BOT_CONFIG::suggestKeys)
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
                        .suggests(BOT_CONFIG::suggestKeys)
                        .then(argument("group", StringArgumentType.greedyString())
                            .suggests(BOT_GROUP_CONFIG::suggestKeys)
                            .executes(BotCommand::groupAddBot)
                        )
                    )
                )
                .then(literal("remove")
                    .then(argument("bot", StringArgumentType.string())
                        .suggests(BOT_CONFIG::suggestKeys)
                        .then(argument("group", StringArgumentType.greedyString())
                            .suggests(BOT_GROUP_CONFIG::suggestKeys)
                            .executes(BotCommand::groupRemoveBot)
                        )
                    )
                )
                .then(literal("load")
                    .then(argument("group", StringArgumentType.greedyString())
                        .suggests(BOT_GROUP_CONFIG::suggestKeys)
                        .executes(BotCommand::groupLoadBot)
                    )
                )
                .then(literal("unload")
                    .then(argument("group", StringArgumentType.greedyString())
                        .suggests(BOT_GROUP_CONFIG::suggestKeys)
                        .executes(BotCommand::groupUnloadBot)
                    )
                )
                .then(literal("info")
                    .then(argument("group", StringArgumentType.greedyString())
                        .suggests(BOT_GROUP_CONFIG::suggestKeys)
                        .executes(BotCommand::groupInfo)
                    )
                )
            )
            .then(literal("action")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandBotAction))
                .then(argument("player", StringArgumentType.string())
                    .suggests(BOT_CONFIG::suggestKeys)
                    .executes(BotCommand::actionList)
                    .then(literal("list")
                        .executes(BotCommand::actionList)
                        .then(argument("page", IntegerArgumentType.integer(1))
                            .executes(BotCommand::actionList)
                        )
                    )
                    .then(literal("add")
                        .then(actionAddCommand(dispatcher))
                    )
                    .then(literal("remove")
                        .then(argument("id", LongArgumentType.longArg())
                            .suggests(BotCommand::suggestActions)
                            .executes(BotCommand::actionRemove)
                        )
                    )
                    .then(literal("startup")
                        .then(literal("toggle")
                            .then(argument("id", LongArgumentType.longArg())
                                .suggests(BotCommand::suggestActions)
                                .executes(BotCommand::actionStartupToggle)
                                .then(argument("value", BoolArgumentType.bool())
                                    .executes(BotCommand::actionStartupToggle)
                                )
                            )
                        )
                        .then(literal("clear")
                            .executes(BotCommand::actionStartupClear)
                        )
                    )
                )
            )
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> actionAddCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        ArgumentBuilder<CommandSourceStack, ?> node = argument("desc", StringArgumentType.string());
        CommandNode<CommandSourceStack> playerOperateNode = dispatcher.getRoot()
            .getChild("player")
            .getChild("player");
        loopCommand(node, playerOperateNode, BotCommand::actionAdd);
        return node;
    }

    private static void loopCommand(ArgumentBuilder<CommandSourceStack, ?> node, CommandNode<CommandSourceStack> parent, Command<CommandSourceStack> execute) {
        for (CommandNode<CommandSourceStack> child : parent.getChildren()) {
            ArgumentBuilder<CommandSourceStack, ?> builder = child.createBuilder();
            if (builder.getCommand() != null) {
                builder.executes(execute);
            }
            loopCommand(builder, child, execute);
            node.then(builder);
        }
    }

    private static void tryInit(CommandContext<CommandSourceStack> context) {
        BOT_CONFIG.tryInit(context);
        BOT_GROUP_CONFIG.tryInit(context);
    }

    private static CompletableFuture<Suggestions> suggestActions(
        final CommandContext<CommandSourceStack> context,
        final SuggestionsBuilder builder
    ) {
        tryInit(context);
        String name = StringArgumentType.getString(context, "player");
        BotInfo bot = BOT_CONFIG.get(name);
        List<String> actionIds = bot == null ?
            List.of() :
            bot.executors().stream()
            .map(it -> String.valueOf(it.id()))
            .toList();
        return SharedSuggestionProvider.suggest(actionIds, builder);
    }

    @Nullable
    public static BotInfo getBotInfo(MinecraftServer server, String name) {
        BOT_CONFIG.tryInit(server);
        return BOT_CONFIG.get(name);
    }

    // Bot

    @Nullable
    private static BotInfo getBot(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String name = StringArgumentType.getString(context, "player");
        BotInfo bot = BOT_CONFIG.get(name);
        if (bot == null) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("Bot %s is not exist.", name));
        }
        return bot;
    }

    @Nullable
    private static Pair<BotInfo, BotExecutorInfo> getBotAction(CommandContext<CommandSourceStack> context) {
        BotInfo bot = getBot(context);
        if (bot == null) return null;
        long id = LongArgumentType.getLong(context, "id");
        BotExecutorInfo executor = bot.executors().stream()
            .filter(it -> it.id() == id)
            .findFirst()
            .orElse(null);
        if (executor == null) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("Action id %s is not found for bot %s", id, bot.name()));
            return null;
        }

        return Pair.of(bot, executor);
    }

    private static int load(CommandContext<CommandSourceStack> context) {
        BotInfo bot = getBot(context);
        if (bot == null) return 0;
        return spawnBot(context.getSource(), bot) ? Command.SINGLE_SUCCESS : 0;
    }

    private static boolean spawnBot(CommandSourceStack source, BotInfo bot) {
        //#if MC>=12002
        if (
            //#if MC < 12104
            BotUtil.isGcaSpawningBot(bot.name())
            //#else
            //$$ EntityPlayerMPFake.isSpawningPlayer(bot.name())
            //#endif
        ) {
            source.sendFailure(ComponentHelper.fmtHlt("Player %s is currently logging on", bot.name()));
            return false;
        }
        //#endif
        if (source.getServer().getPlayerList().getPlayerByName(bot.name()) != null) {
            source.sendFailure(ComponentHelper.fmtHlt("player %s is already exist.", bot.name()));
            return false;
        }
        boolean success = BotUtil.spawnBot(source.getServer(), bot);
        if (!success) {
            source.sendFailure(ComponentHelper.fmtHlt("%s is not loaded.", bot.name()));
        }
        return success;

    }

    private static int list(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        PageInfo<BotInfo> page = PageInfo.of(context, BOT_CONFIG.values());
        if (page == null) return 0;
        String actionPermission = Boolean.toString(CommandHelper.canUseCommand(context.getSource(), GcaSetting.commandBotAction));
        page.sendPageInfo(context, "msg.gca.bot.list", "/bot list", actionPermission);
        return Command.SINGLE_SUCCESS;
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        tryInit(context);
        CommandSourceStack source = context.getSource();
        ServerPlayer p;
        if (!((p = EntityArgument.getPlayer(context, "player")) instanceof EntityPlayerMPFake player)) {
            source.sendFailure(ComponentHelper.fmtHlt("%s is not a fake player.", p.getGameProfile().getName()));
            return 0;
        }
        String name = player.getGameProfile().getName();
        if (BOT_CONFIG.contains(name)) {
            source.sendFailure(ComponentHelper.fmtHlt("%s is already save.", name));
            return 0;
        }
        BOT_CONFIG.update(BotInfo.create(
            name,
            StringArgumentType.getString(context, "desc"),
            player,
            ((ServerPlayerInterface) player).getActionPack()
        ));
        source.sendSuccess(() -> ComponentHelper.fmtHlt("%s is added.", name), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String name = StringArgumentType.getString(context, "player");
        if (BOT_CONFIG.remove(name) == null) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("Bot %s is not exist.", name));
            return 0;
        }
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("%s is removed.", name), false);
        return Command.SINGLE_SUCCESS;
    }

    // Group

    @Nullable
    private static GroupNode getGroupNode(CommandContext<CommandSourceStack> context, String groupName) {
        tryInit(context);
        BotGroupInfo groupInfo = BOT_GROUP_CONFIG.get(groupName);
        if (groupInfo == null) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("Group %s is not found.", groupName));
            return null;
        }
        List<BotInfo> bots = groupInfo.bots().stream()
            .map(BOT_CONFIG::get)
            .filter(Objects::nonNull)
            .toList();
        if (bots.size() != groupInfo.bots().size()) {
            BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, bots.stream().map(BotInfo::name).toList()));
        }
        return new GroupNode(groupInfo, bots);
    }

    private static int groupList(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        PageInfo<BotGroupInfo> page = PageInfo.of(context, BOT_GROUP_CONFIG.values());
        if (page == null) return 0;
        page.sendPageInfo(context, "msg.gca.bot.group.list", "/bot group list");
        return Command.SINGLE_SUCCESS;
    }

    private static int groupCreate(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String groupName = StringArgumentType.getString(context, "name");
        if (BOT_GROUP_CONFIG.contains(groupName)) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("Group %s already exists.", groupName));
            return 0;
        }
        BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, List.of()));
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("Group %s created successfully.", groupName), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupRemove(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String groupName = StringArgumentType.getString(context, "name");
        if (BOT_GROUP_CONFIG.remove(groupName) == null) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("Bot Group %s is not exist.", groupName));
            return 0;
        }
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("%s is removed.", groupName), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupInfo(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        PageInfo<BotInfo> page = PageInfo.ofAll(context, group.bots);
        if (page == null) return 0;
        String actionPermission = Boolean.toString(CommandHelper.canUseCommand(context.getSource(), GcaSetting.commandBotAction));
        page.sendPageInfo(
            context,
            Pair.of("msg.gca.bot.group.list.bot", new Object[]{groupName}),
            "/bot group info " + groupName,
            actionPermission
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int groupAddBot(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        String botName = StringArgumentType.getString(context, "bot");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        List<String> bots = new ArrayList<>(group.group.bots());
        if (bots.contains(botName)) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("Bot %s is already added.", botName));
            return 0;
        }
        bots.add(botName);
        BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, bots));
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("Bot %s is added to %s successfully.", botName, groupName), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupRemoveBot(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        String botName = StringArgumentType.getString(context, "bot");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        List<String> bots = new ArrayList<>(group.group.bots());
        if (!bots.remove(botName)) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("Bot %s is not found in the %s.", botName, groupName));
            return 0;
        }
        BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, bots));
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("Bot %s is removed from %s successfully.", botName, groupName), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupLoadBot(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        CommandSourceStack source = context.getSource();
        return (int) group.bots.stream().filter(it -> spawnBot(source, it)).count();
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

    private static int groupGenerated(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String groupName = StringArgumentType.getString(context, "name");
        int groupCount = IntegerArgumentType.getInteger(context, "count");
        boolean groupLoad = BoolArgumentType.getBool(context, "load");
        CommandSourceStack source = context.getSource();
        if (BOT_GROUP_CONFIG.contains(groupName)) {
            source.sendFailure(ComponentHelper.fmtHlt("Group %s already exists.", groupName));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command source must be player."));
            return 0;
        }

        List<BotInfo> bots = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            String botName = "bot_%s_%s".formatted(groupName, i);
            if (BOT_CONFIG.contains(botName)) {
                source.sendFailure(ComponentHelper.fmtHlt("Bot %s already exists.", botName));
                continue;
            }
            BotInfo bot = BotInfo.create(botName, botName, player, null);
            BOT_CONFIG.update(bot, false);
            bots.add(bot);
        }
        BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, bots.stream().map(BotInfo::name).toList()));
        if (groupLoad) {
            for (BotInfo bot : bots) {
                spawnBot(source, bot);
            }
        }
        source.sendSuccess(() -> ComponentHelper.fmtHlt("Group %s generated successfully.", groupName), false);
        return bots.size();
    }

    // Action

    private static int actionList(CommandContext<CommandSourceStack> context) {
        BotInfo bot = getBot(context);
        if (bot == null) return 0;
        PageInfo<BotExecutorInfo> page = PageInfo.of(context, bot.executors());
        if (page == null) return 0;
        page.sendPageInfo(
            context,
            Pair.of("Bot %s's Action List", new Object[]{bot.name()}),
            "/bot action " + bot.name() + " list",
            bot.name()
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int actionAdd(CommandContext<CommandSourceStack> context) {
        BotInfo bot = getBot(context);
        if (bot == null) return 0;
        Matcher matcher = ACTION_PATTERN.matcher(context.getInput());
        if (!matcher.matches()) {
            context.getSource().sendFailure(Component.literal("Invalid command syntax."));
            return 0;
        }
        String action = matcher.group(3).trim();
        long id = IdUtil.nextId();
        String desc = StringArgumentType.getString(context, "desc");
        List<BotExecutorInfo> executors = new ArrayList<>(bot.executors());
        executors.add(new BotExecutorInfo(id, desc, action, false));
        BOT_CONFIG.update(bot.withExecutors(executors));
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("%s is added.", desc), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int actionRemove(CommandContext<CommandSourceStack> context) {
        Pair<BotInfo, BotExecutorInfo> pair = getBotAction(context);
        if (pair == null) return 0;
        BotInfo bot = pair.getLeft();
        BotExecutorInfo action = pair.getRight();
        List<BotExecutorInfo> executors = new ArrayList<>(bot.executors());
        executors.removeIf(it -> it.id() == action.id());
        BOT_CONFIG.update(bot.withExecutors(executors));
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("%s is removed.", action.desc()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int actionStartupToggle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Pair<BotInfo, BotExecutorInfo> pair = getBotAction(context);
        if (pair == null) return 0;
        BotInfo bot = pair.getLeft();
        BotExecutorInfo action = pair.getRight();
        boolean set = CommandUtil.getArgOrDefault(
            () -> BoolArgumentType.getBool(context, "value"),
            () -> !action.startup()
        );
        if (action.startup() == set) {
            context.getSource().sendFailure(ComponentHelper.fmtHlt("%s is already %s as startup action for %s.", action.desc(), set ? "set" : "not set", bot.name()));
            return 0;
        }
        BotExecutorInfo newAction = action.withStartup(set);
        List<BotExecutorInfo> executors = new ArrayList<>(bot.executors());
        for (int i = 0; i < executors.size(); i++) {
            if (executors.get(i).id() == newAction.id()) {
                executors.set(i, newAction);
                break;
            }
        }
        BOT_CONFIG.update(bot.withExecutors(executors));
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("%s is now %s as startup action for %s.", action.desc(), set ? "set" : "not set", bot.name()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int actionStartupClear(CommandContext<CommandSourceStack> context) {
        BotInfo bot = getBot(context);
        if (bot == null) return 0;
        List<BotExecutorInfo> executors = bot.executors().stream()
            .map(it -> it.withStartup(false))
            .toList();
        BOT_CONFIG.update(bot.withExecutors(executors));
        context.getSource().sendSuccess(() -> ComponentHelper.fmtHlt("%s's startup action has been cleared.", bot.name()), false);
        return Command.SINGLE_SUCCESS;
    }

    record GroupNode(BotGroupInfo group, List<BotInfo> bots) {
    }
}
