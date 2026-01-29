package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.FilesUtil;
import dev.dubhe.gugle.carpet.tools.GameProfileHelper;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class BlistCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable(
        "commands.ban.failed"));
    private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable(
        "commands.pardon.failed"));
    public static final FilesUtil.MapFile<String, Boolean> PERMISSION = new FilesUtil.MapFile<>("blist", Object::toString, Boolean.class);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "blist")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandBlist) && WlistCommand.hasPermission(
                    PERMISSION,
                    stack
                ))
                .executes(BlistCommand::list)
                .then(
                    Commands.literal("permission")
                        .requires(stack -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .executes(BlistCommand::permissionAdd)
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .executes(BlistCommand::permissionRemove)
                                )
                        )
                )
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("targets", GameProfileArgument.gameProfile())
                                .executes(BlistCommand::add)
                                .then(
                                    Commands.argument("reson", StringArgumentType.greedyString())
                                        .executes(BlistCommand::add)
                                )
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("targets", GameProfileArgument.gameProfile())
                                .suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
                                    commandContext.getSource()
                                        .getServer()
                                        .getPlayerList()
                                        .getBans()
                                        .getUserList(), suggestionsBuilder
                                ))
                                .executes(BlistCommand::remove)
                        )
                )
        );
    }

    public static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        UserBanList userBanList = source.getServer().getPlayerList().getBans();
        AtomicInteger i = new AtomicInteger();
        Component component = null;
        try {
            component = Component.literal(StringArgumentType.getString(context, "reson"));
        } catch (IllegalArgumentException ignored) {
        }
        Component finalComponent = component;
        GameProfileHelper.praseGameProfileCollection(
            context, "targets", (gameProfile, name, id) -> {
                if (!userBanList.isBanned(gameProfile)) {
                    UserBanListEntry userBanListEntry = new UserBanListEntry(
                        gameProfile,
                        null,
                        source.getTextName(),
                        null,
                        finalComponent == null ? null : finalComponent.getString()
                    );
                    userBanList.add(userBanListEntry);
                    i.incrementAndGet();
                    source.sendSuccess(
                        () -> Component.translatable(
                            "commands.ban.success",
                            Component.literal(name),
                            userBanListEntry.getReason()
                        ), true
                    );
                    ServerPlayer serverPlayer = source.getServer().getPlayerList().getPlayer(id);
                    if (serverPlayer != null) {
                        serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
                    }
                }
            }
        );

        if (i.get() == 0) {
            throw ERROR_ALREADY_BANNED.create();
        } else {
            return i.get();
        }
    }

    public static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        UserBanList userBanList = source.getServer().getPlayerList().getBans();
        AtomicInteger i = new AtomicInteger();
        GameProfileHelper.praseGameProfileCollection(
            context, "targets", (gameProfile, name, id) -> {
                if (userBanList.isBanned(gameProfile)) {
                    if (userBanList.isBanned(gameProfile)) {
                        userBanList.remove(gameProfile);
                        i.incrementAndGet();
                        source.sendSuccess(() -> Component.translatable("commands.pardon.success", Component.literal(name)), true);
                    }
                }
            }
        );
        if (i.get() == 0) {
            throw ERROR_NOT_BANNED.create();
        } else {
            return i.get();
        }
    }

    public static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Collection<UserBanListEntry> collection = source.getServer().getPlayerList().getBans().getEntries();
        if (collection.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.banlist.none"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.banlist.list", collection.size()), false);

            for (UserBanListEntry userBanListEntry : collection) {
                source.sendSuccess(
                    () -> Component.translatable(
                        "commands.banlist.entry",
                        userBanListEntry.getDisplayName(),
                        userBanListEntry.getSource(),
                        userBanListEntry.getReason()
                    ), false
                );
            }
        }
        return collection.size();
    }

    private static int permissionAdd(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PERMISSION.init(context);
        AtomicInteger i = new AtomicInteger();
        GameProfileHelper.praseGameProfileCollection(
            context, "targets", (gameProfile, name, id) -> {
                PERMISSION.map.put(id.toString(), true);
                context.getSource()
                    .sendSuccess(
                        () -> Component.literal("Player %s has been granted permission to operate the banned list.".formatted(name)),
                        true
                    );
                i.incrementAndGet();
            }
        );
        PERMISSION.save();
        return i.get();
    }

    private static int permissionRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PERMISSION.init(context);
        AtomicInteger i = new AtomicInteger();
        GameProfileHelper.praseGameProfileCollection(
            context, "targets", (gameProfile, name, id) -> {
                PERMISSION.map.put(id.toString(), false);
                context.getSource()
                    .sendSuccess(
                        () -> Component.literal("Revoked player %s's permission to operate the banned list".formatted(name)),
                        true
                    );
                i.incrementAndGet();
            }
        );
        PERMISSION.save();
        return i.get();
    }
}
