package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.ComponentUtils;
import dev.dubhe.gugle.carpet.tools.FilesUtil;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.tools.PosUtils;
import dev.dubhe.gugle.carpet.tools.IdGenerator;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LocCommand {
    public static final FilesUtil.MapFile<Long, LocPoint> LOC_POINT = new FilesUtil.MapFile<>("loc", Long::decode, LocPoint.class);

    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
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

    private static @NotNull CompletableFuture<Suggestions> suggestId(
        final CommandContext<CommandSourceStack> context,
        final SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggest(LOC_POINT.map.keySet().stream().map(Object::toString), builder);
    }

    public static int add(CommandContext<CommandSourceStack> context) {
        LOC_POINT.init(context);
        CommandSourceStack source = context.getSource();
        long id = IdGenerator.nextId();
        String desc = StringArgumentType.getString(context, "desc");
        Vec3 pos = source.getPosition();
        ResourceKey<Level> dim = source.getLevel().dimension();
        LOC_POINT.map.put(id, new LocPoint(id, desc, pos.x, pos.y, pos.z, dim));
        LOC_POINT.save();
        source.sendSuccess(() -> Component.literal("Loc %s is added.".formatted(desc)), false);
        return 1;
    }

    public static int remove(CommandContext<CommandSourceStack> context) {
        LOC_POINT.init(context);
        Long id = LongArgumentType.getLong(context, "id");
        LocPoint remove = LOC_POINT.map.remove(id);
        if (remove == null) {
            context.getSource().sendFailure(Component.literal("No such loc id %s".formatted(id)));
            return 0;
        }
        LOC_POINT.save();
        context.getSource().sendSuccess(() -> Component.literal("Loc %s is removed.".formatted(remove.desc)), false);
        return 1;
    }

    public static int list(CommandContext<CommandSourceStack> context) {
        LOC_POINT.init(context);
        int page;
        try {
            page = IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {
            page = 1;
        }
        final int pageSize = 8;
        int size = LOC_POINT.map.size();
        int maxPage = size / pageSize + 1;
        if (page > maxPage) {
            context.getSource().sendFailure(Component.literal("No such page %s".formatted(page)));
            return 0;
        }
        LocPoint[] locPoints = LOC_POINT.map.values().toArray(new LocPoint[0]);
        context.getSource().sendSystemMessage(
            Component.literal("======= Loc List (Page %s/%s) =======".formatted(page, maxPage))
                .withStyle(ChatFormatting.YELLOW)
        );
        for (int i = (page - 1) * pageSize; i < size && i < page * pageSize; i++) {
            context.getSource().sendSystemMessage(locToComponent(locPoints[i]));
        }
        Component prevPage = page <= 1 ?
            Component.literal("<<<").withStyle(ChatFormatting.GRAY) :
            Component.literal("<<<").withStyle(
                Style.EMPTY
                    .applyFormat(ChatFormatting.GREEN)
                    .withClickEvent(ComponentUtils.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/loc list " + (page - 1)))
            );
        Component nextPage = page >= maxPage ?
            Component.literal(">>>").withStyle(ChatFormatting.GRAY) :
            Component.literal(">>>").withStyle(
                Style.EMPTY
                    .applyFormat(ChatFormatting.GREEN)
                    .withClickEvent(ComponentUtils.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/loc list " + (page + 1)))
            );
        context.getSource().sendSystemMessage(
            Component.literal("=======")
                .withStyle(ChatFormatting.YELLOW)
                .append(" ")
                .append(prevPage)
                .append(" ")
                .append(Component.literal("(Loc %s/%s)".formatted(page, maxPage)).withStyle(ChatFormatting.YELLOW))
                .append(" ")
                .append(nextPage)
                .append(" ")
                .append(Component.literal("=======").withStyle(ChatFormatting.YELLOW))
        );
        return 1;
    }

    private static @NotNull MutableComponent locToComponent(@NotNull LocPoint locPoint) {
        MutableComponent component = Component.literal(locPoint.desc).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtils.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(Long.toString(locPoint.id))))
        );
        List<MutableComponent> pos = PosUtils.pos(locPoint.desc, locPoint.x, locPoint.y, locPoint.z, locPoint.dimType);
        MutableComponent info = Component.literal("[i]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.YELLOW)
                .withHoverEvent(ComponentUtils.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("View loc point information")))
                .withClickEvent(ComponentUtils.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/loc info %s".formatted(locPoint.id)))
        );
        MutableComponent remove = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtils.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove loc point")))
                .withClickEvent(ComponentUtils.createClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/loc remove %s".formatted(locPoint.id)))
        );
        return Component.literal("▶ ").append(component)
            .append(" ").append(pos.getFirst())
            .append(" ").append(info)
            .append(" ").append(remove);
    }

    public static int info(CommandContext<CommandSourceStack> context) {
        LOC_POINT.init(context);
        Long id = LongArgumentType.getLong(context, "id");
        LocPoint point = LOC_POINT.map.getOrDefault(id, null);
        if (point == null) {
            context.getSource().sendFailure(Component.literal("No such loc id %s".formatted(id)));
            return 0;
        }
        for (Component component : LocCommand.info(point)) {
            context.getSource().sendSuccess(() -> component, false);
        }
        return 1;
    }

    public static @NotNull List<Component> info(@NotNull LocPoint point) {
        MutableComponent desc = Component.literal(point.desc);
        MutableComponent dimType;
        if (point.dimType == Level.NETHER) {
            dimType = Component.translatableWithFallback("advancements.nether.root.title", point.dimType.location().toString());
        } else if (point.dimType == Level.END) {
            dimType = Component.translatableWithFallback("advancements.end.root.title", point.dimType.location().toString());
        } else if (point.dimType == Level.OVERWORLD) {
            dimType = Component.translatableWithFallback("flat_world_preset.minecraft.overworld", point.dimType.location().toString());
        } else {
            dimType = Component.literal(point.dimType.location().toString());
        }
        List<MutableComponent> pos = PosUtils.pos(point.desc, point.x, point.y, point.z, point.dimType);
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

    public record LocPoint(
        long id,
        String desc,
        double x,
        double y,
        double z,
        @SerializedName("dim_type") ResourceKey<Level> dimType
    ) {
    }
}
