package dev.dubhe.gugle.carpet.util;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Component;

import java.net.URI;
//#if MC >= 12106
//$$ import net.minecraft.resources.ResourceLocation;
//$$ import java.util.Optional;
//#endif
//#if MC >= 260000
//$$ import net.minecraft.world.item.ItemStackTemplate;
//#else
import net.minecraft.world.item.ItemStack;
//#endif

public class ComponentUtil {
    public static <T> HoverEvent createHoverEvent(HoverEvent.Action action, T object) {
        try {
            return switch (action) {
                case SHOW_ITEM -> new HoverEvent.ShowItem((
                    //#if MC >= 260000
                    //$$ ItemStackTemplate
                    //#else
                    ItemStack
                    //#endif
                    ) object);
                case SHOW_ENTITY -> new HoverEvent.ShowEntity((HoverEvent.EntityTooltipInfo) object);
                case SHOW_TEXT -> new HoverEvent.ShowText((Component) object);
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ClickEvent createClickEvent(ClickEvent.Action action, String string) {
        try {
            return switch (action) {
                case OPEN_URL -> new ClickEvent.OpenUrl(new URI(string));
                case OPEN_FILE -> new ClickEvent.OpenFile(string);
                case RUN_COMMAND -> new ClickEvent.RunCommand(string);
                case SUGGEST_COMMAND -> new ClickEvent.SuggestCommand(string);
                case COPY_TO_CLIPBOARD -> new ClickEvent.CopyToClipboard(string);
                case CHANGE_PAGE -> new ClickEvent.ChangePage(Integer.parseInt(string));
                //#if MC >= 12106
                //$$ default -> new ClickEvent.Custom(ResourceLocation.parse(string), Optional.empty());
                //#endif
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
