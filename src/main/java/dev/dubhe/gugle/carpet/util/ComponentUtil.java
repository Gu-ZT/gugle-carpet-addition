package dev.dubhe.gugle.carpet.util;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

public class ComponentUtil {
    public static <T> HoverEvent createHoverEvent(HoverEvent.Action<T> action, T object) {
        return new HoverEvent(action, object);
    }

    public static ClickEvent createClickEvent(ClickEvent.Action action, String string) {
        return new ClickEvent(action, string);
    }
}
