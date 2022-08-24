package dev.dubhe.gugle.carpet.api.menu;

import com.mojang.datafixers.util.Pair;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.api.menu.control.ButtonList;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomMenu implements Container {

    public final List<Pair<Integer, Button>> buttons = new ArrayList<>();
    public final List<ButtonList> buttonLists = new ArrayList<>();

    public void tick() {
        this.checkButton();
    }

    public void addButton(int slot, Button button) {
        if (getContainerSize() < (slot + 1)) {
            return;
        }
        buttons.add(new Pair<>(slot, button));
    }

    public void addButtonList(ButtonList buttonList) {
        this.buttonLists.add(buttonList);
    }

    private void checkButton() {
        for (Pair<Integer, Button> button : buttons) {
            button.getSecond().checkButton(this, button.getFirst());
        }
    }
}

