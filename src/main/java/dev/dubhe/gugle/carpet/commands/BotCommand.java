package dev.dubhe.gugle.carpet.commands;

import carpet.CarpetSettings;
import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import carpet.utils.CommandHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.BotGroupInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import dev.dubhe.gugle.carpet.tools.GameProfileHelper;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerSerializer;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

//#if MC>=12100
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
//#endif
//#if MC>=12102
//$$ import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
//$$ import java.util.Set;
//#else
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
//#endif
//#if MC>=12110
//$$ import dev.dubhe.gugle.carpet.mixin.EntityPlayerMPFakeInvoker;
//$$ import net.minecraft.server.players.NameAndId;
//#elseif MC>=12100
import net.minecraft.world.level.block.entity.SkullBlockEntity;
//#endif

public class BotCommand {
    public static final GcaConfig<BotInfo> BOT_CONFIG = GcaConfig.create("bot", BotInfo.CODEC);
    public static final GcaConfig<BotGroupInfo> BOT_GROUP_CONFIG = GcaConfig.create("botGroup", BotGroupInfo.CODEC);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "bot")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandBot))
                .executes(BotCommand::list)
                .then(
                    Commands.literal("list").executes(BotCommand::list)
                        .then(
                            Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(BotCommand::list)
                        )
                )
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("player", EntityArgument.player())
                                .then(
                                    Commands.argument("desc", StringArgumentType.greedyString())
                                        .executes(BotCommand::add)
                                )
                        )
                )
                .then(
                    Commands.literal("load")
                        .then(
                            Commands.argument("player", StringArgumentType.string())
                                .suggests(BotCommand::suggestPlayer)
                                .executes(BotCommand::load)
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("player", StringArgumentType.string())
                                .suggests(BotCommand::suggestPlayer)
                                .executes(BotCommand::remove)
                        )
                )
                .then(
                    Commands.literal("group")
                        .executes(BotCommand::groupList)
                        .then(
                            Commands.literal("create")
                                .then(
                                    Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(BotCommand::groupCreate)
                                )
                        )
                        .then(
                            Commands.literal("generated")
                                .then(
                                    Commands.argument("name", StringArgumentType.word())
                                        .executes(BotCommand::groupGenerated)
                                        .then(
                                            Commands.argument("count", IntegerArgumentType.integer(1, 32))
                                                .then(
                                                    Commands.argument("load", BoolArgumentType.bool())
                                                        .executes(BotCommand::groupGenerated)
                                                )
                                                .executes(BotCommand::groupGenerated)
                                        )
                                )
                        )
                        .then(
                            Commands.literal("list").executes(BotCommand::groupList)
                                .then(
                                    Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(BotCommand::groupList)
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(BotCommand::groupRemove)
                                )
                        )
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("bot", StringArgumentType.string())
                                        .suggests(BotCommand::suggestPlayer)
                                        .then(
                                            Commands.argument("group", StringArgumentType.greedyString())
                                                .suggests(BotCommand::suggestGroup)
                                                .executes(BotCommand::groupAddBot)
                                        )
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("bot", StringArgumentType.string())
                                        .suggests(BotCommand::suggestPlayer)
                                        .then(
                                            Commands.argument("group", StringArgumentType.greedyString())
                                                .suggests(BotCommand::suggestGroup)
                                                .executes(BotCommand::groupRemoveBot)
                                        )
                                )
                        )
                        .then(
                            Commands.literal("load")
                                .then(
                                    Commands.argument("group", StringArgumentType.greedyString())
                                        .suggests(BotCommand::suggestGroup)
                                        .executes(BotCommand::groupLoadBot)
                                )
                        )
                        .then(
                            Commands.literal("unload")
                                .then(
                                    Commands.argument("group", StringArgumentType.greedyString())
                                        .suggests(BotCommand::suggestGroup)
                                        .executes(BotCommand::groupUnloadBot)
                                )
                        )
                        .then(
                            Commands.literal("info")
                                .then(
                                    Commands.argument("group", StringArgumentType.greedyString())
                                        .suggests(BotCommand::suggestGroup)
                                        .executes(BotCommand::groupInfo)
                                )
                        )
                )
        );
    }

    private static void tryInit(CommandContext<CommandSourceStack> context) {
        BOT_CONFIG.tryInit(context);
        BOT_GROUP_CONFIG.tryInit(context);
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

    private static int getPage(CommandContext<CommandSourceStack> context) {
        try {
            return IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {
            return 1;
        }
    }

    private static int groupInfo(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        final int pageSize = 8;
        int page = getPage(context);
        int size = group.bots().size();
        int maxPage = size / pageSize + 1;
        if (page > maxPage) {
            context.getSource().sendFailure(Component.literal("No such page %s".formatted(page)));
            return 0;
        }
        context.getSource().sendSystemMessage(
            Component.literal("======= Bot Group %s (Page %s/%s) =======".formatted(groupName, page, maxPage))
                .withStyle(ChatFormatting.YELLOW)
        );
        for (int i = (page - 1) * pageSize; i < size && i < page * pageSize; i++) {
            context.getSource().sendSystemMessage(botToComponent(group.bots.get(i)));
        }
        listComponent(context, page, maxPage, "/bot group show");
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
        for (BotInfo bot : group.bots) {
            load(source, bot);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int groupRemoveBot(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        String botName = StringArgumentType.getString(context, "bot");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        CommandSourceStack source = context.getSource();
        List<String> bots = group.group.bots();
        if (!bots.remove(botName)) {
            source.sendFailure(Component.literal("Bot %s is not found in the %s.".formatted(botName, groupName)));
            return 0;
        }
        BOT_GROUP_CONFIG.update(group.group);
        source.sendSuccess(() -> Component.literal("Bot %s is removed from %s successfully.".formatted(botName, groupName)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int groupAddBot(CommandContext<CommandSourceStack> context) {
        String groupName = StringArgumentType.getString(context, "group");
        String botName = StringArgumentType.getString(context, "bot");
        GroupNode group = getGroupNode(context, groupName);
        if (group == null) return 0;
        CommandSourceStack source = context.getSource();
        List<String> bots = group.group.bots();
        if (bots.contains(botName)) {
            source.sendFailure(Component.literal("Bot %s is already added.".formatted(botName)));
            return 0;
        }
        bots.add(botName);
        BOT_GROUP_CONFIG.update(group.group);
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
            BotInfo bot = new BotInfo(
                botName,
                botName,
                player.position(),
                player.getRotationVector(),
                player.level().dimension(),
                player.gameMode.getGameModeForPlayer(),
                player.getAbilities().flying,
                FakePlayerSerializer.actionPackToJson(new EntityPlayerActionPack(player))
            );
            BOT_CONFIG.update(bot, false);
            bots.add(bot);
        }
        BOT_GROUP_CONFIG.update(new BotGroupInfo(groupName, bots.stream().map(BotInfo::name).toList()));
        if (groupLoad) {
            for (BotInfo bot : bots) {
                load(source, bot);
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
        List<BotGroupInfo> groupInfos = BOT_GROUP_CONFIG.getContents().values().stream().toList();
        final int pageSize = 8;
        int page = getPage(context);
        int size = groupInfos.size();
        int maxPage = size / pageSize + 1;
        if (page > maxPage) {
            context.getSource().sendFailure(Component.literal("No such page %s".formatted(page)));
            return 0;
        }
        context.getSource().sendSystemMessage(
            Component.literal("======= Bot Group List (Page %s/%s) =======".formatted(page, maxPage))
                .withStyle(ChatFormatting.YELLOW)
        );
        for (int i = (page - 1) * pageSize; i < size && i < page * pageSize; i++) {
            context.getSource().sendSystemMessage(botGroupToComponent(groupInfos.get(i)));
        }
        listComponent(context, page, maxPage, "/bot group list");
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

    private static boolean load(CommandSourceStack source, @Nullable BotInfo bot) {
        if (bot == null) {
            source.sendFailure(Component.literal("%s is not exist."));
            return false;
        }

        if (source.getServer().getPlayerList().getPlayerByName(bot.name()) != null) {
            source.sendFailure(Component.literal("player %s is already exist.".formatted(bot.name())));
            return false;
        }
        source.getServer().getLevel(bot.dimType());
        try {
            ServerLevel worldIn = source.getServer().getLevel(bot.dimType());
            //#if MC < 12110
            GameProfileCache.setUsesAuthentication(false);
            try {
                //#endif
                GameProfileCache profileCache = GameProfileHelper.getProfileCache(source.getServer());
                //#if MC >= 12110
                //$$ NameAndId
                //#else
                GameProfile
                    //#endif
                    gameprofile = getGameProfile(profileCache, bot.name());
                if (gameprofile == null) return false;
                //#if MC>=12100
                //#if MC>=12110
                //$$ GameProfileHelper.fetchGameProfile(source.getServer(), gameprofile.name())
                //#elseif MC>=12100
                SkullBlockEntity.fetchGameProfile(gameprofile.getName())
                //#endif
                    .thenAcceptAsync(
                        (
                            //#if MC >= 12110
                            //$$ current
                            //#else
                            p
                                //#endif
                        ) -> {
                            //#if MC<12110
                            GameProfile current = p.orElse(gameprofile);
                            //#endif
                            if (worldIn == null) return;
                            EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(
                                source.getServer(),
                                worldIn,
                                current,
                                ClientInformation.createDefault()
                            );
                            instance.fixStartingPosition = () -> instance.moveTo(
                                bot.pos().x,
                                bot.pos().y,
                                bot.pos().z,
                                bot.facing().y,
                                bot.facing().x
                            );
                            source.getServer().getPlayerList()
                                .placeNewPlayer(
                                    new FakeClientConnection(PacketFlow.SERVERBOUND),
                                    instance,
                                    new CommonListenerCookie(current, 0, instance.clientInformation(), false)
                                );
                            //#if MC>=12110
                            //$$ EntityPlayerMPFakeInvoker.invokeLoadPlayerData(instance);
                            //#endif
                            //#if MC>=12102
                            //$$ instance.teleportTo(worldIn, bot.pos().x, bot.pos().y, bot.pos().z, Set.of(), bot.facing().y, bot.facing().x, true);
                            //#else
                            instance.teleportTo(worldIn, bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
                            //#endif
                            instance.setHealth(20.0F);
                            ((EntityInvoker) instance).invokeUnsetRemoved();
                            AttributeInstance attribute = instance.getAttribute(Attributes.STEP_HEIGHT);
                            if (attribute != null) attribute.setBaseValue(0.6000000238418579);
                            instance.gameMode.changeGameModeForPlayer(bot.mode());
                            source.getServer().getPlayerList()
                                .broadcastAll(
                                    new ClientboundRotateHeadPacket(
                                        instance,
                                        (byte) ((int) (instance.yHeadRot * 256.0F / 360.0F))
                                    ), bot.dimType()
                                );
                            //#if MC>=12102
                            //$$ source.getServer().getPlayerList().broadcastAll(ClientboundEntityPositionSyncPacket.of(instance), bot.dimType());
                            //#else
                            source.getServer().getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(instance), bot.dimType());
                            //#endif
                            instance.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 127);
                            instance.getAbilities().flying = bot.flying();
                            FakePlayerSerializer.applyActionPackFromJson(bot.actions(), instance);
                        }, source.getServer()
                    );
                //#else
                //$$ if (worldIn == null) return false;
                //$$ EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(source.getServer(), worldIn, gameprofile);
                //$$ instance.fixStartingPosition = () -> instance.moveTo(bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
                //$$ source.getServer().getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), instance);
                //$$ instance.teleportTo(worldIn, bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
                //$$ instance.setHealth(20.0F);
                //$$ ((EntityInvoker) instance).invokeUnsetRemoved();
                //$$ instance.setMaxUpStep(0.6F);
                //$$ instance.gameMode.changeGameModeForPlayer(bot.mode());
                //$$ source.getServer().getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(instance, (byte)((int)(instance.yHeadRot * 256.0F / 360.0F))), bot.dimType());
                //$$ source.getServer().getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(instance), bot.dimType());
                //$$ instance.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte)127);
                //$$ instance.getAbilities().flying = bot.flying();
                //$$ FakePlayerSerializer.applyActionPackFromJson(bot.actions(), instance);
                //#endif
                //#if MC < 12110
            } finally {
                GameProfileCache.setUsesAuthentication(source.getServer().isDedicatedServer() && source.getServer().usesAuthentication());
            }
            //#endif
            return true;
        } catch (Exception e) {
            GcaExtension.LOGGER.error("Failed to load bot: {}", bot.name(), e);
            source.sendFailure(Component.literal("%s is not loaded.".formatted(bot.name())));
            return false;
        }
    }

    //#if MC>=12110
    //$$ @Nullable
    //$$ private static NameAndId getGameProfile(@Nullable CachedUserNameToIdResolver cache, String name) {
    //$$     NameAndId profile = null;
    //$$     if (cache != null) {
    //$$         cache.resolveOfflineUsers(true);
    //$$         profile = cache.get(name).orElse(null);
    //$$         cache.resolveOfflineUsers(BOT_CONFIG.getServer().isDedicatedServer() && BOT_CONFIG.getServer().usesAuthentication());
    //$$     }
    //$$     if (profile == null && CarpetSettings.allowSpawningOfflinePlayers) {
    //$$         profile = new NameAndId(UUIDUtil.createOfflinePlayerUUID(name), name);
    //$$     }
    //$$     return profile;
    //$$ }
    //#else
    @Nullable
    private static GameProfile getGameProfile(@Nullable GameProfileCache cache, String name) {
        GameProfile gameprofile = cache == null ? null : cache.get(name).orElse(null);
        if (gameprofile == null && CarpetSettings.allowSpawningOfflinePlayers) {
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        }
        return gameprofile;
    }
    //#endif

    private static int load(CommandContext<CommandSourceStack> context) {
        tryInit(context);
        String name = StringArgumentType.getString(context, "player");
        BotInfo bot = BOT_CONFIG.getContents().get(name);
        return load(context.getSource(), bot) ? Command.SINGLE_SUCCESS : 0;
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        tryInit(context);
        CommandSourceStack source = context.getSource();
        ServerPlayer p;
        if (!((p = EntityArgument.getPlayer(context, "player")) instanceof EntityPlayerMPFake player)) {
            source.sendFailure(Component.literal("%s is not a fake player.".formatted(
                p.getGameProfile()
                    //#if MC>=12110
                    //$$ .name()
                    //#else
                    .getName()
                    //#endif
            )));
            return 0;
        }
        String name = player.getGameProfile()
            //#if MC>=12110
            //$$ .name();
            //#else
            .getName();
            //#endif
        if (BOT_CONFIG.getContents().containsKey(name)) {
            source.sendFailure(Component.literal("%s is already save.".formatted(name)));
            return 0;
        }
        BOT_CONFIG.update(new BotInfo(
            name,
            StringArgumentType.getString(context, "desc"),
            player.position(),
            player.getRotationVector(),
            player.level().dimension(),
            player.gameMode.getGameModeForPlayer(),
            player.getAbilities().flying,
            FakePlayerSerializer.actionPackToJson(((ServerPlayerInterface) player).getActionPack())
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
        List<BotInfo> bots = BOT_CONFIG.getContents().values().stream().toList();
        final int pageSize = 8;
        int page = getPage(context);
        int size = bots.size();
        int maxPage = size / pageSize + 1;
        if (page > maxPage) {
            context.getSource().sendFailure(Component.literal("No such page %s".formatted(page)));
            return 0;
        }
        context.getSource().sendSystemMessage(
            Component.literal("======= Bot List (Page %s/%s) =======".formatted(page, maxPage))
                .withStyle(ChatFormatting.YELLOW)
        );
        for (int i = (page - 1) * pageSize; i < size && i < page * pageSize; i++) {
            context.getSource().sendSystemMessage(botToComponent(bots.get(i)));
        }
        listComponent(context, page, maxPage, "/bot list");
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

    private static void listComponent(CommandContext<CommandSourceStack> context, int page, int maxPage, String command) {
        Component prevPage = page <= 1 ?
            Component.literal("<<<").withStyle(ChatFormatting.GRAY) :
            Component.literal("<<<").withStyle(
                Style.EMPTY
                    .applyFormat(ChatFormatting.GREEN)
                    .withClickEvent(ComponentUtil.createClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        command + " " + (page - 1)
                    ))
            );
        Component nextPage = page >= maxPage ?
            Component.literal(">>>").withStyle(ChatFormatting.GRAY) :
            Component.literal(">>>").withStyle(
                Style.EMPTY
                    .applyFormat(ChatFormatting.GREEN)
                    .withClickEvent(ComponentUtil.createClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        command + " " + (page + 1)
                    ))
            );
        context.getSource().sendSystemMessage(
            Component.literal("=======")
                .withStyle(ChatFormatting.YELLOW)
                .append(" ")
                .append(prevPage)
                .append(" ")
                .append(Component.literal("(Page %s/%s)".formatted(page, maxPage)).withStyle(ChatFormatting.YELLOW))
                .append(" ")
                .append(nextPage)
                .append(" ")
                .append(Component.literal("=======").withStyle(ChatFormatting.YELLOW))
        );
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
