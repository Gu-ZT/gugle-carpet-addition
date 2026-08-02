package dev.dubhe.gugle.carpet.api.menu;

import com.mojang.datafixers.util.Pair;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.api.menu.control.ButtonBuilder;
import dev.dubhe.gugle.carpet.api.menu.control.ButtonList;
import dev.dubhe.gugle.carpet.api.menu.control.ButtonV2;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomMenu implements Container {
    protected final List<Pair<Integer, Button>> buttons = new ArrayList<>();
    protected final List<ButtonList> buttonLists = new ArrayList<>();
    protected final List<ButtonV2> buttonsV2 = new ArrayList<>(54);

    public void tick() {
        this.tickButtons();
        this.checkButton();
    }

    public void addButton(int slot, Button button) {
        if (getContainerSize() < (slot + 1)) {
            return;
        }
        this.buttons.add(Pair.of(slot, button));
    }

    public void addButton(int slot, ButtonBuilder builder) {
        if (this.getContainerSize() < (slot + 1)) {
            return;
        }
        this.buttonsV2.add(builder.build(this, slot));
    }

    public void addButtonList(ButtonList buttonList) {
        this.buttonLists.add(buttonList);
    }

    public Container selfUpdate() {
        return this;
    }

    private void tickButtons() {
        List<ButtonV2> clicked = this.buttonsV2.stream().filter(ButtonV2::clicked).toList();

        if (!clicked.isEmpty() && clicked.size() < 3) {
            for (ButtonV2 button : clicked) {
                button.clickCallback();
            }
        }

        for (ButtonV2 button : this.buttonsV2) {
            button.refresh();
        }
    }

    private void checkButton() {
        List<Pair<Integer, Button>> operates = this.buttons.stream()
            .filter(it -> it.getSecond().check(this, it.getFirst()))
            .toList();

        if (operates.isEmpty()) return;

        if (operates.size() < 3) {
            for (Pair<Integer, Button> button : operates) {
                button.getSecond().execute(this, button.getFirst());
            }
        }

        for (Pair<Integer, Button> button : this.buttons) {
            button.getSecond().updateButton(this, button.getFirst());
        }
    }
}

