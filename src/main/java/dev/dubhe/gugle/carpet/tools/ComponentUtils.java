package dev.dubhe.gugle.carpet.tools;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;
//#if MC > 12104
//$$ import net.minecraft.network.chat.Component;
//$$ import net.minecraft.world.item.ItemStack;
//$$ import java.net.URI;
//#endif

public class ComponentUtils {
    //#if MC <= 12104
    public static <T> @NotNull HoverEvent createHoverEvent(HoverEvent.Action<T> action, T object) {
        return new HoverEvent(action, object);
    }

    public static @NotNull ClickEvent createClickEvent(ClickEvent.Action action, String string) {
        return new ClickEvent(action, string);
    }
    //#else
    //$$ public static <T> @NotNull HoverEvent createHoverEvent(@NotNull HoverEvent.Action action, T object) {
    //$$     try {
    //$$         return switch (action) {
    //$$             case SHOW_ITEM -> new HoverEvent.ShowItem((ItemStack) object);
    //$$             case SHOW_ENTITY -> new HoverEvent.ShowEntity((HoverEvent.EntityTooltipInfo) object);
    //$$             case SHOW_TEXT -> new HoverEvent.ShowText((Component) object);
    //$$         };
    //$$     } catch (Exception e) {
    //$$         throw new RuntimeException(e);
    //$$     }
    //$$ }
    //$$
    //$$ public static @NotNull ClickEvent createClickEvent(ClickEvent.Action action, String string) {
    //$$     try {
    //$$         return switch (action) {
    //$$             case OPEN_URL -> new ClickEvent.OpenUrl(new URI(string));
    //$$             case OPEN_FILE -> new ClickEvent.OpenFile(string);
    //$$             case RUN_COMMAND -> new ClickEvent.RunCommand(string);
    //$$             case SUGGEST_COMMAND -> new ClickEvent.SuggestCommand(string);
    //$$             case COPY_TO_CLIPBOARD -> new ClickEvent.CopyToClipboard(string);
    //$$             case CHANGE_PAGE -> new ClickEvent.ChangePage(Integer.parseInt(string));
    //$$         };
    //$$     } catch (Exception e) {
    //$$         throw new RuntimeException(e);
    //$$     }
    //$$ }
    //#endif
}
