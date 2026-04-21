package dev.dubhe.gugle.carpet.api.menu.control;

import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class AutoResetButton extends Button {
    public AutoResetButton(String key) {
        super(false,
            ComponentHelper.tr(key, Color.WHITE, Style.EMPTY.withBold(true).withItalic(false)),
            ComponentHelper.tr(key, Color.WHITE, Style.EMPTY.withBold(true).withItalic(false))
        );
        this.addTurnOnFunction(this::turnOffWithoutFunction);
    }

    public AutoResetButton(String key, Item item) {
        super(false,
            item,
            item,
            1,
            ComponentHelper.tr(key, Color.WHITE, Style.EMPTY.withBold(true).withItalic(false)),
            ComponentHelper.tr(key, Color.WHITE, Style.EMPTY.withBold(true).withItalic(false))
        );
        this.addTurnOnFunction(this::turnOffWithoutFunction);
    }

    public static final AutoResetButton NONE = new AutoResetButton("gca.button.none", Items.RED_STAINED_GLASS_PANE);
}
