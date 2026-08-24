package dev.dubhe.gugle.carpet.api.menu;

import dev.dubhe.gugle.carpet.api.menu.control.ButtonBuilder;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import net.minecraft.world.Container;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CustomMenu implements Container {
    protected final List<Button> buttons = new ArrayList<>(54);

    public void tick() {
        this.tickButtons();
    }

    @Nullable
    public Button addButton(int slot, ButtonBuilder builder) {
        if (this.getContainerSize() < (slot + 1)) {
            return null;
        }
        Button button = builder.build(this, slot);
        this.buttons.add(button);
        return button;
    }

    public Container selfUpdate() {
        return this;
    }

    private void tickButtons() {
        List<Button> clicked = this.buttons.stream().filter(Button::clicked).toList();

        if (!clicked.isEmpty() && clicked.size() < 3) {
            for (Button button : clicked) {
                button.clickCallback();
            }
        }

        for (Button button : this.buttons) {
            button.refresh();
        }
    }
}

