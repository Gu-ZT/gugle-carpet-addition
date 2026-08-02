package dev.dubhe.gugle.carpet.api.menu.control;

import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class AutoResetButton extends Button {
    private static final Style STYLE = Style.EMPTY.withBold(true).withItalic(false);

    private AutoResetButton(Component on, Component off) {
        super(false, on, off);
        this.addTurnOnFunction(this::turnOffWithoutFunction);
    }

    private AutoResetButton(Component on, Component off, Item item) {
        super(false, item, item, 1, on, off);
        this.addTurnOnFunction(this::turnOffWithoutFunction);
    }

    public static AutoResetButton ofName(String name) {
        Component text = Component.literal(name);
        return new AutoResetButton(text, text);
    }

    public static AutoResetButton ofKey(String key) {
        Component text = ComponentHelper.tr(key, Color.WHITE, STYLE);
        return new AutoResetButton(text, text);
    }

    public static AutoResetButton ofKey(String key, Item item) {
        Component text = ComponentHelper.tr(key, Color.WHITE, STYLE);
        return new AutoResetButton(text, text, item);
    }

    public static final AutoResetButton NONE = AutoResetButton.ofKey("gca.button.none",
        //#if MC < 260200
        Items.RED_STAINED_GLASS_PANE
        //#else
        //$$ Items.STAINED_GLASS_PANE.red()
        //#endif
    );
}
