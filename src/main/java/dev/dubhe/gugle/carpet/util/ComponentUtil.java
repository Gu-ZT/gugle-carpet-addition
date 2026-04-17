package dev.dubhe.gugle.carpet.util;

import dev.dubhe.gugle.carpet.entry.BotExecuterInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;

public class ComponentUtil {
    public static <T> HoverEvent createHoverEvent(HoverEvent.Action<T> action, T object) {
        return new HoverEvent(action, object);
    }

    public static ClickEvent createClickEvent(ClickEvent.Action action, String string) {
        return new ClickEvent(action, string);
    }

    public static Component botActionComponent(MinecraftServer server, BotExecuterInfo action) {
        Component desc = Component.literal(action.desc()).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(action.action())))
        );
        Component execute = Component.literal("[▶]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Execute action")))
                .withClickEvent(createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot %s action execute %d".formatted(action.bot(), action.id())
                ))
        );
        Component delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove action")))
                .withClickEvent(createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot %s action remove %d".formatted(action.bot(), action.id())
                ))
        );
        MutableComponent component = Component.literal("[%d] ".formatted(action.id())).append(desc);
        component.append(" ").append(execute);
        return component.append(" ").append(delete);
    }
}
