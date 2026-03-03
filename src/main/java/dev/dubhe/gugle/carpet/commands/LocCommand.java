package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.LocationInfo;
import dev.dubhe.gugle.carpet.entry.PageInfo;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.util.PosUtil;
import dev.dubhe.gugle.carpet.util.IdUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LocCommand {
    private static final GcaConfig<LocationInfo> LOCATION_CONFIG = GcaConfig.create("loc", LocationInfo.CODEC);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "loc")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandLoc))
                .executes(LocCommand::list)
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("desc", StringArgumentType.greedyString())
                                .executes(LocCommand::add)
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("id", LongArgumentType.longArg())
                                .suggests(LocCommand::suggestId)
                                .executes(LocCommand::remove)
                        )
                )
                .then(
                    Commands.literal("info")
                        .then(
                            Commands.argument("id", LongArgumentType.longArg())
                                .suggests(LocCommand::suggestId)
                                .executes(LocCommand::info)
                        )
                )
                .then(
                    Commands.literal("list")
                        .executes(LocCommand::list)
                        .then(
                            Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(LocCommand::list)
                        )
                )
        );
    }

    private static CompletableFuture<Suggestions> suggestId(
        final CommandContext<CommandSourceStack> context,
        final SuggestionsBuilder builder
    ) {
        LOCATION_CONFIG.tryInit(context);
        return SharedSuggestionProvider.suggest(LOCATION_CONFIG.getContents().keySet(), builder);
    }

    public static int add(CommandContext<CommandSourceStack> context) {
        LOCATION_CONFIG.tryInit(context);
        CommandSourceStack source = context.getSource();
        long id = IdUtil.nextId();
        String desc = StringArgumentType.getString(context, "desc");
        Vec3 pos = source.getPosition();
        ResourceKey<Level> dimension = source.getLevel().dimension();
        LOCATION_CONFIG.update(new LocationInfo(id, desc, pos, dimension));
        source.sendSuccess(() -> Component.literal("Loc %s is added.".formatted(desc)), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int remove(CommandContext<CommandSourceStack> context) {
        LOCATION_CONFIG.tryInit(context);
        long id = LongArgumentType.getLong(context, "id");
        LocationInfo removed = LOCATION_CONFIG.remove(String.valueOf(id));
        if (removed == null) {
            context.getSource().sendFailure(Component.literal("No such loc id %s".formatted(id)));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal("Loc %s is removed.".formatted(removed.desc())), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int list(CommandContext<CommandSourceStack> context) {
        LOCATION_CONFIG.tryInit(context);
        PageInfo<LocationInfo> page = PageInfo.of(context, LOCATION_CONFIG.getContents().values());
        if (page == null) return 0;
        context.getSource().sendSystemMessage(
            Component.literal("======= Loc List (Page %s/%s) =======".formatted(page.pageNum(), page.maxPage()))
                .withStyle(ChatFormatting.YELLOW)
        );
        for (LocationInfo node : page.page()) {
            context.getSource().sendSystemMessage(locToComponent(node));
        }
        Component prevPage = page.pageNum() <= 1 ?
                             Component.literal("<<<").withStyle(ChatFormatting.GRAY) :
                             Component.literal("<<<").withStyle(
                                 Style.EMPTY
                                     .applyFormat(ChatFormatting.GREEN)
                                     .withClickEvent(ComponentUtil.createClickEvent(
                                         ClickEvent.Action.RUN_COMMAND,
                                         "/loc list " + (page.pageNum() - 1)
                                     ))
                             );
        Component nextPage = page.pageNum() >= page.maxPage() ?
                             Component.literal(">>>").withStyle(ChatFormatting.GRAY) :
                             Component.literal(">>>").withStyle(
                                 Style.EMPTY
                                     .applyFormat(ChatFormatting.GREEN)
                                     .withClickEvent(ComponentUtil.createClickEvent(
                                         ClickEvent.Action.RUN_COMMAND,
                                         "/loc list " + (page.pageNum() + 1)
                                     ))
                             );
        context.getSource().sendSystemMessage(
            Component.literal("=======")
                .withStyle(ChatFormatting.YELLOW)
                .append(" ")
                .append(prevPage)
                .append(" ")
                .append(Component.literal("(Loc %s/%s)".formatted(page.pageNum(), page.maxPage())).withStyle(ChatFormatting.YELLOW))
                .append(" ")
                .append(nextPage)
                .append(" ")
                .append(Component.literal("=======").withStyle(ChatFormatting.YELLOW))
        );
        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent locToComponent(LocationInfo loc) {
        MutableComponent component = Component.literal(loc.desc()).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(loc.name())))
        );
        List<MutableComponent> pos = PosUtil.pos(loc.desc(), loc.pos(), loc.dimType());
        MutableComponent info = Component.literal("[i]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.YELLOW)
                .withHoverEvent(ComponentUtil.createHoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.literal("View loc point information")
                ))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/loc info %s".formatted(loc.name())))
        );
        MutableComponent remove = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove loc point")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/loc remove %s".formatted(loc.id())))
        );
        return Component.literal("▶ ").append(component)
            .append(" ").append(pos.getFirst())
            .append(" ").append(info)
            .append(" ").append(remove);
    }

    public static int info(CommandContext<CommandSourceStack> context) {
        LOCATION_CONFIG.tryInit(context);
        long id = LongArgumentType.getLong(context, "id");
        LocationInfo location = LOCATION_CONFIG.getContents().get(String.valueOf(id));
        if (location == null) {
            context.getSource().sendFailure(Component.literal("No such loc id %s".formatted(id)));
            return 0;
        }
        for (Component component : LocCommand.info(location)) {
            context.getSource().sendSuccess(() -> component, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static List<Component> info(LocationInfo loc) {
        MutableComponent desc = Component.literal(loc.desc());
        MutableComponent dimType;
        if (loc.dimType() == Level.NETHER) {
            dimType = Component.translatableWithFallback("advancements.nether.root.title", loc.dimType().location().toString());
        } else if (loc.dimType() == Level.END) {
            dimType = Component.translatableWithFallback("advancements.end.root.title", loc.dimType().location().toString());
        } else if (loc.dimType() == Level.OVERWORLD) {
            dimType = Component.translatableWithFallback("flat_world_preset.minecraft.overworld", loc.dimType().location().toString());
        } else {
            dimType = Component.literal(loc.dimType().location().toString());
        }
        List<MutableComponent> pos = PosUtil.pos(loc.desc(), loc.pos(), loc.dimType());
        List<Component> result = new ArrayList<>();
        result.add(Component.literal("==================").withStyle(ChatFormatting.YELLOW));
        result.add(Component.literal("Loc Point: ").append(desc));
        result.add(Component.literal("Dimension: ").append(dimType));
        if (!pos.isEmpty()) result.add(Component.literal("Position: ").append(pos.get(0)));
        if (pos.size() > 1) result.add(pos.get(1));
        if (pos.size() > 2) result.add(Component.literal("Transform Position: ").append(pos.get(2)));
        if (pos.size() > 3) result.add(pos.get(3));
        result.add(Component.literal("==================").withStyle(ChatFormatting.YELLOW));
        return result;
    }
}
