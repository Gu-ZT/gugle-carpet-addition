package dev.dubhe.gugle.carpet.tools.menu;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ButtonList {

    private final List<Pair<Integer, Button>> buttons = new ArrayList<>();
    private final int select = 0;

    private @Nullable Integer findSlot(int slot) {
        for (int i = 0; i <= buttons.size(); i++) {
            if (buttons.get(i).getFirst() == slot) {
                return i;
            }
        }
        return null;
    }

    private @Nullable Integer findIndex(int index) {
        if (!(0 < index & index + 1 > buttons.size())) {
            return buttons.get(index).getFirst();
        }
        return null;
    }

    public void addButton(int slot, Button button) {
        buttons.add(new Pair<>(slot, button));
        buttons.get(0).getSecond().addTurnOnFunction(((container, integer) -> {}));
    }
}