package dev.dubhe.gugle.carpet.util;

import dev.dubhe.gugle.carpet.entry.BotGroupInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.entry.LocationInfo;
import dev.dubhe.gugle.carpet.entry.TodoInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class ComponentUtil {
    public static <T> HoverEvent createHoverEvent(HoverEvent.Action<T> action, T object) {
        return new HoverEvent(action, object);
    }

    public static ClickEvent createClickEvent(ClickEvent.Action action, String string) {
        return new ClickEvent(action, string);
    }

    public static Component botComponent(MinecraftServer server, BotInfo bot) {
        Component desc = Component.literal(bot.desc()).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(bot.name())))
        );
        boolean notOnline = server.getPlayerList().getPlayerByName(bot.name()) == null;
        Component load = Component.literal("[↑]").withStyle(
            Style.EMPTY
                .applyFormat(notOnline ? ChatFormatting.GREEN : ChatFormatting.GRAY)
                .withHoverEvent(createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Load bot")))
                .withClickEvent(createClickEvent(ClickEvent.Action.RUN_COMMAND, "/bot load %s".formatted(bot.name())))
        );
        Component remove = Component.literal("[↓]").withStyle(
            Style.EMPTY
                .applyFormat(notOnline ? ChatFormatting.GRAY : ChatFormatting.RED)
                .withHoverEvent(createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Unload bot")))
                .withClickEvent(createClickEvent(ClickEvent.Action.RUN_COMMAND, "/player %s kill".formatted(bot.name())))
        );
        Component delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove bot")))
                .withClickEvent(createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot remove %s".formatted(bot.name())
                ))
        );
        MutableComponent component = Component.literal("▶ ")
            .withStyle(notOnline ? ChatFormatting.RED : ChatFormatting.GREEN)
            .append(desc);
        component.append(" ").append(load);
        component.append(" ").append(remove);
        return component.append(" ").append(delete);
    }

    public static Component botGroupComponent(MinecraftServer server, BotGroupInfo group) {
        Component name = Component.literal(group.name()).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(group.name())))
        );
        Component load = Component.literal("[↑]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Load Group")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot group load %s".formatted(group.name())
                ))
        );
        Component remove = Component.literal("[↓]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Unload Group")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot group unload %s".formatted(group.name())
                ))
        );
        Component info = Component.literal("[i]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Group Info")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot group info %s".formatted(group.name())
                ))
        );
        Component delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove Bot Group")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot group remove %s".formatted(group.name())
                ))
        );
        MutableComponent component = Component.literal("▶ ").append(name);
        component.append(" ").append(load);
        component.append(" ").append(remove);
        component.append(" ").append(info);
        return component.append(" ").append(delete);
    }

    public static Component locationComponent(MinecraftServer server, LocationInfo location) {
        Component component = Component.literal(location.desc()).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(location.name())))
        );
        List<MutableComponent> pos = PosUtil.pos(location.desc(), location.pos(), location.dimension());
        Component info = Component.literal("[i]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.YELLOW)
                .withHoverEvent(ComponentUtil.createHoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.literal("View loc point information")
                ))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/loc info %s".formatted(location.name())))
        );
        Component remove = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove loc point")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/loc remove %s".formatted(location.id())))
        );
        return Component.literal("▶ ").append(component)
            .append(" ").append(pos.getFirst())
            .append(" ").append(info)
            .append(" ").append(remove);
    }

    public static Component todoComponent(MinecraftServer server, TodoInfo todo) {
        Component component = Component.literal(todo.desc()).withStyle(
            Style.EMPTY
                .withStrikethrough(todo.success())
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(todo.name())))
        );
        Component success = Component.literal("[✔]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Make todo done")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/todo success %s".formatted(todo.id())))
        );
        Component unSuccess = Component.literal("[❌]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Make todo undone")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/todo success %s false".formatted(todo.id())))
        );
        Component remove = Component.literal("[\uD83D\uDDD1]").withStyle(
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
