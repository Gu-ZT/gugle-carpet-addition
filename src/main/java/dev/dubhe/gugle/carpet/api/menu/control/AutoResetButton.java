package dev.dubhe.gugle.carpet.api.menu.control;

import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import net.minecraft.network.chat.Style;

public class AutoResetButton extends Button {

    public AutoResetButton(String key) {
        super(false,
                ComponentTranslate.trans(key, Color.WHITE, Style.EMPTY.withBold(true).withItalic(false)),
                ComponentTranslate.trans(key, Color.WHITE, Style.EMPTY.withBold(true).withItalic(false))
        );
        this.addTurnOnFunction(this::turnOffWithoutFunction);
    }
}
