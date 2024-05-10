package dev.dubhe.gugle.carpet.api.menu;

import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.api.menu.control.ButtonList;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class CustomMenu implements Container {
    public final List<Map.Entry<Integer, Button>> buttons = new ArrayList<>();
    public final List<ButtonList> buttonLists = new ArrayList<>();

    public void tick() {
        this.checkButton();
    }

    public void addButton(int slot, Button button) {
        if (getContainerSize() < (slot + 1)) {
            return;
        }
        buttons.add(Map.entry(slot, button));
    }

    public void addButtonList(ButtonList buttonList) {
        this.buttonLists.add(buttonList);
    }

    private void checkButton() {
        for (Map.Entry<Integer, Button> button : buttons) {
            button.getValue().checkButton(this, button.getKey());
        }
    }
}

