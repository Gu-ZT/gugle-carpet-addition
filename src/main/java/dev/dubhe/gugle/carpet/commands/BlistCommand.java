package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.NameBooleanInfo;
import dev.dubhe.gugle.carpet.entry.PlayerGameProfileInfo;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.util.CommandUtil;
import dev.dubhe.gugle.carpet.util.PermissionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class BlistCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable(
        "commands.ban.failed"));
    private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable(
        "commands.pardon.failed"));

    private static final GcaConfig<NameBooleanInfo> BLIST_PERMISSION_CONFIG = GcaConfig.create("blist", NameBooleanInfo.CODEC);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "blist")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandBlist) && PermissionUtil.hasPermission(
                    BLIST_PERMISSION_CONFIG,
                    stack
                ))
                .executes(BlistCommand::list)
                .then(
                    Commands.literal("permission")
                        .requires(stack ->
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
                                    Commands.argument("reason", StringArgumentType.greedyString())
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

    @Nullable
    private static String getReason(CommandContext<CommandSourceStack> context) {
        try {
            return StringArgumentType.getString(context, "reason");
        } catch (IllegalArgumentException ignored) { }
        return null;
    }

    public static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        UserBanList banned = source.getServer().getPlayerList().getBans();
        String reason = getReason(context);
        int counter = 0;
        List<PlayerGameProfileInfo> profiles = CommandUtil.parseGameProfiles(context, "targets");
        for (PlayerGameProfileInfo info : profiles) {
            if (banned.isBanned(info.profile())) continue;
            UserBanListEntry ban = new UserBanListEntry(info.profile(), null, source.getTextName(), null, reason);
            banned.add(ban);
            counter++;
            ServerPlayer serverPlayer = source.getServer().getPlayerList().getPlayer(info.uuid());
            if (serverPlayer != null) {
                serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }
            source.sendSuccess(() -> Component.translatable("commands.ban.success", Component.literal(info.name()), ban.getReason()), true);
        }
        if (counter == 0) throw ERROR_ALREADY_BANNED.create();
        return counter;
    }

    public static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        UserBanList banned = source.getServer().getPlayerList().getBans();
        int counter = 0;
        List<PlayerGameProfileInfo> profiles = CommandUtil.parseGameProfiles(context, "targets");
        for (PlayerGameProfileInfo info : profiles) {
            if (!banned.isBanned(info.profile())) continue;
            banned.remove(info.profile());
            counter++;
            source.sendSuccess(() -> Component.translatable("commands.pardon.success", Component.literal(info.name())), true);
        }
        if (counter == 0) throw ERROR_NOT_BANNED.create();
        return counter;
    }

    public static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Collection<UserBanListEntry> collection = source.getServer().getPlayerList().getBans().getEntries();
        if (collection.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.banlist.none"), false);
            return 0;
        }
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
        return collection.size();
    }

    private static int permissionAdd(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BLIST_PERMISSION_CONFIG.tryInit(context);
        int counter = 0;
        List<PlayerGameProfileInfo> profiles = CommandUtil.parseGameProfiles(context, "targets");
        for (PlayerGameProfileInfo info : profiles) {
            BLIST_PERMISSION_CONFIG.update(new NameBooleanInfo(info.uuid().toString(), true), false);
            counter++;
            context.getSource()
                .sendSuccess(
                    () -> Component.literal("Player %s has been granted permission to operate the banned list.".formatted(info.name())),
                    true
                );
        }
        if (counter > 0) BLIST_PERMISSION_CONFIG.setDirty();
        return counter;
    }

    private static int permissionRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BLIST_PERMISSION_CONFIG.tryInit(context);
        int counter = 0;
        List<PlayerGameProfileInfo> profiles = CommandUtil.parseGameProfiles(context, "targets");
        for (PlayerGameProfileInfo info : profiles) {
            BLIST_PERMISSION_CONFIG.update(new NameBooleanInfo(info.uuid().toString(), false), false);
            counter++;
            context.getSource()
                .sendSuccess(
                    () -> Component.literal("Revoked player %s's permission to operate the banned list".formatted(info.name())),
                    true
                );
        }
        if (counter > 0) BLIST_PERMISSION_CONFIG.setDirty();
        return counter;
    }

}
