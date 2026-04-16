package dev.dubhe.gugle.carpet.commands;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.LocationInfo;
import dev.dubhe.gugle.carpet.entry.PageInfo;
import dev.dubhe.gugle.carpet.util.CommandUtil;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import dev.dubhe.gugle.carpet.tools.ModCommands;
import dev.dubhe.gugle.carpet.util.PosUtil;
import dev.dubhe.gugle.carpet.util.IdUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
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

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class LocCommand {
    private static final GcaConfig<LocationInfo> LOCATION_CONFIG = GcaConfig.create("location", LocationInfo.CODEC);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ModCommands.root(dispatcher, "loc")
                .requires(stack -> CommandHelper.canUseCommand(stack, GcaSetting.commandLoc))
                .executes(LocCommand::list)
                .then(literal("add")
                    .then(locAppendCommand())
                    .then(literal("at").then(argument("pos", Vec3Argument.vec3())
                        .then(locAppendCommand())
                        .then(literal("in").then(argument("dimension", DimensionArgument.dimension())
                            .then(locAppendCommand())
                        ))
                    ))
                )
                .then(literal("remove")
                    .then(argument("id", LongArgumentType.longArg())
                        .suggests(LOCATION_CONFIG::suggestKeys)
                        .executes(LocCommand::remove)
                    )
                )
                .then(literal("info")
                    .then(argument("id", LongArgumentType.longArg())
                        .suggests(LOCATION_CONFIG::suggestKeys)
                        .executes(LocCommand::info)
                    )
                )
                .then(literal("list").executes(LocCommand::list)
                    .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(LocCommand::list)
                    )
                )
        );
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> locAppendCommand() {
        return argument("desc", StringArgumentType.greedyString())
            .executes(LocCommand::add);
    }

    public static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LOCATION_CONFIG.tryInit(context);
        CommandSourceStack source = context.getSource();
        Vec3 pos = CommandUtil.getArgOrDefault(
            () -> Vec3Argument.getVec3(context, "pos"),
            source::getPosition
        );
        ResourceKey<Level> dimension = CommandUtil.getArgOrDefault(
            () -> DimensionArgument.getDimension(context, "dimension").dimension(),
            source.getLevel()::dimension
        );
        long id = IdUtil.nextId();
        String desc = StringArgumentType.getString(context, "desc");
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
        page.pageComponents("Loc List", "/loc list", LocCommand::locToComponent)
            .forEach(context.getSource()::sendSystemMessage);
        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent locToComponent(LocationInfo loc) {
        MutableComponent component = Component.literal(loc.desc()).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(loc.name())))
        );
        List<MutableComponent> pos = PosUtil.pos(loc.desc(), loc.pos(), loc.dimension());
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
        MutableComponent dimension = getDimensionComponent(loc.dimension());
        List<MutableComponent> pos = PosUtil.pos(loc.desc(), loc.pos(), loc.dimension());
        List<Component> result = new ArrayList<>();
        result.add(Component.literal("==================").withStyle(ChatFormatting.YELLOW));
        result.add(Component.literal("Loc Point: ").append(desc));
        result.add(Component.literal("Dimension: ").append(dimension));
        if (!pos.isEmpty()) result.add(Component.literal("Position: ").append(pos.get(0)));
        if (pos.size() > 1) result.add(pos.get(1));
        if (pos.size() > 2) result.add(Component.literal("Transform Position: ").append(pos.get(2)));
        if (pos.size() > 3) result.add(pos.get(3));
        result.add(Component.literal("==================").withStyle(ChatFormatting.YELLOW));
        return result;
    }

    private static MutableComponent getDimensionComponent(ResourceKey<Level> dimension) {
        String name = dimension
            //#if MC>=12111
            //$$ .identifier()
            //#else
            .location()
            //#endif
            .toString();
        if (dimension == Level.NETHER) {
            return Component.translatableWithFallback("advancements.nether.root.title", name);
        } else if (dimension == Level.END) {
            return Component.translatableWithFallback("advancements.end.root.title", name);
        } else if (dimension == Level.OVERWORLD) {
            return Component.translatableWithFallback("flat_world_preset.minecraft.overworld", name);
        } else {
            return Component.literal(name);
        }
    }
}
