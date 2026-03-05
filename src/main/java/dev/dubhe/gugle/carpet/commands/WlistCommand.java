package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.NameBooleanInfo;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.tools.GameProfileHelper;
import dev.dubhe.gugle.carpet.util.PermissionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;

import java.util.concurrent.atomic.AtomicInteger;

public class WlistCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_WHITELISTED = new SimpleCommandExceptionType(Component.translatable(
        "commands.whitelist.add.failed"));
    private static final SimpleCommandExceptionType ERROR_NOT_WHITELISTED = new SimpleCommandExceptionType(Component.translatable(
        "commands.whitelist.remove.failed"));

    private static final GcaConfig<NameBooleanInfo> WLIST_PERMISSION_CONFIG = GcaConfig.create("wlist", NameBooleanInfo.CODEC);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "wlist")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandWlist) && PermissionUtil.hasPermission(
                    WLIST_PERMISSION_CONFIG,
                    stack
                ))
                .executes(WlistCommand::list)
                .then(
                    Commands.literal("permission")
                        .requires(stack ->
                            //
                            //#if MC>=12111
                            //$$ Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(stack)
                            //#else
                            stack.hasPermission(Commands.LEVEL_GAMEMASTERS)
                            //#endif
                        )
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .executes(WlistCommand::permissionAdd)
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .executes(WlistCommand::permissionRemove)
                                )
                        )
                )
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("targets", GameProfileArgument.gameProfile())
                                .suggests((commandContext, suggestionsBuilder) -> {
                                    PlayerList playerList = commandContext.getSource().getServer().getPlayerList();
                                    return SharedSuggestionProvider.suggest(
                                        playerList.getPlayers()
                                            .stream()
                                            .filter((serverPlayer) -> !playerList.getWhiteList()
                                                .isWhiteListed(GameProfileHelper.prasePlayerGameProfile(serverPlayer)))
                                            .map(GameProfileHelper::prasePlayerGameName),
                                        suggestionsBuilder
                                    );
                                })
                                .executes(WlistCommand::add)
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
                                        .getWhiteListNames(), suggestionsBuilder
                                ))
                                .executes(WlistCommand::remove)
                        )
                )
        );
    }

    public static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        UserWhiteList whiteList = source.getServer().getPlayerList().getWhiteList();
        AtomicInteger counter = new AtomicInteger();
        GameProfileHelper.praseGameProfileCollection(
            context, "targets", (profile, name, uuid) -> {
                if (whiteList.isWhiteListed(profile)) return;
                UserWhiteListEntry userWhiteListEntry = new UserWhiteListEntry(profile);
                whiteList.add(userWhiteListEntry);
                counter.getAndIncrement();
                source.sendSuccess(() -> Component.translatable("commands.whitelist.add.success", Component.literal(name)), true);
            }
        );
        if (counter.get() == 0) throw ERROR_ALREADY_WHITELISTED.create();
        return counter.get();
    }

    public static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        UserWhiteList whiteList = source.getServer().getPlayerList().getWhiteList();
        AtomicInteger counter = new AtomicInteger();
        GameProfileHelper.praseGameProfileCollection(
            context, "targets", (profile, name, uuid) -> {
                if (whiteList.isWhiteListed(profile)) {
                    UserWhiteListEntry userWhiteListEntry = new UserWhiteListEntry(profile);
                    whiteList.remove(userWhiteListEntry);
                    counter.getAndIncrement();
                    source.sendSuccess(() -> Component.translatable("commands.whitelist.remove.success", Component.literal(name)), true);
                }
            }
        );
        if (counter.get() == 0) throw ERROR_NOT_WHITELISTED.create();
        source.getServer().kickUnlistedPlayers(
            //#if MC<12109
            source
            //#endif
        );
        return counter.get();
    }

    public static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String[] strings = source.getServer().getPlayerList().getWhiteListNames();
        if (strings.length == 0) {
            source.sendSuccess(() -> Component.translatable("commands.whitelist.none"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.whitelist.list", strings.length, String.join(", ", strings)), false);
        }
        return strings.length;
    }

    private static int permissionAdd(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        WLIST_PERMISSION_CONFIG.tryInit(context);
        AtomicInteger counter = new AtomicInteger();
        GameProfileHelper.praseGameProfileCollection(
            context, "targets", (profile, name, uuid) -> {
                WLIST_PERMISSION_CONFIG.update(new NameBooleanInfo(uuid.toString(), true), false);
                context.getSource()
                    .sendSuccess(
                        () -> Component.literal("Player %s has been granted permission to operate the whitelist.".formatted(name)),
                        true
                    );
                counter.incrementAndGet();
            }
        );
        if (counter.get() > 0) WLIST_PERMISSION_CONFIG.setDirty();
        return counter.get();
    }

    private static int permissionRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        WLIST_PERMISSION_CONFIG.tryInit(context);
        AtomicInteger counter = new AtomicInteger();
        GameProfileHelper.praseGameProfileCollection(
            context, "targets", (profile, name, uuid) -> {
                WLIST_PERMISSION_CONFIG.update(new NameBooleanInfo(uuid.toString(), false), false);
                context.getSource()
                    .sendSuccess(() -> Component.literal("Revoked player %s's permission to operate the whitelist".formatted(name)), true);
                counter.incrementAndGet();
            }
        );
        if (counter.get() > 0) WLIST_PERMISSION_CONFIG.setDirty();
        return counter.get();
    }
}
