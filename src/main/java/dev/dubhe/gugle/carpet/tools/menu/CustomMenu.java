package dev.dubhe.gugle.carpet.tools.menu;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomMenu {

    private final SimpleContainer container;
    private List<Pair<Integer, Button>> buttons;

    public CustomMenu(int rows) {
        this.container = new SimpleContainer(rows * 9);
    }

    public int getContainerSize() {
        return container.getContainerSize();
    }

    public void setItems(int slot_min, int slot_max, NonNullList<ItemStack> itemStacks) {
        for (int i = slot_min; i <= slot_max; i++) {
            int j = i - slot_min;
            if (itemStacks.size() < (j + 1)) {
                break;
            }
            if (i >= getContainerSize()) {
                break;
            }
            setItem(i, itemStacks.get(j));
        }
    }

    public NonNullList<ItemStack> getItems(int slot_min, int slot_max) {
        int count;
        if (slot_max > getContainerSize()) {
            count = getContainerSize() - slot_min + 1;
        } else {
            count = slot_max - slot_min + 1;
        }
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(count, ItemStack.EMPTY);
        for (int i = 0; i < count; i++) {
            itemStacks.set(i, getItem(slot_min + i));
        }
        return itemStacks;
    }

    public @Nullable ItemStack getItem(int slot) {
        if (getContainerSize() < (slot + 1)) {
            return null;
        }
        return container.getItem(slot);
    }

    public void setItem(int slot, ItemStack itemStack) {
        if (getContainerSize() < (slot + 1)) {
            return;
        }
        this.container.setItem(slot, itemStack);
    }

    public void addButton(int slot, Button button) {
        if (getContainerSize() < (slot + 1)) {
            return;
        }
        buttons.add(new Pair<>(slot, button));
    }

    private void checkButton() {
        for (Pair<Integer, Button> i : buttons) {
            i.getSecond().checkButton(this.container, i.getFirst());
        }
    }
}

