package dev.dubhe.gugle.carpet.api.menu;

import com.mojang.datafixers.util.Pair;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.api.menu.control.ButtonList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomMenu {

    public final SimpleContainer container;
    public final List<Pair<Integer, Button>> buttons = new ArrayList<>();
    public final List<ButtonList> buttonLists = new ArrayList<>();
    public final CustomMenuType.Type type;

    public CustomMenu(CustomMenuType.Type type) {
        this.container = new SimpleContainer(CustomMenuType.getCount(type));
        this.type = type;
    }

    public void tick() {
        this.checkButton();
    }

    public int getContainerSize() {
        return container.getContainerSize();
    }

    public SimpleMenuProvider getMenuProvider(Player player, Component name) {
        return new SimpleMenuProvider((i, inv, p) -> CustomMenuType.getMenu(type, player.getId(),
                player.getInventory(), this.container), name);
    }

    public void clearAllItem() {
        for (int i = 0; i < container.getContainerSize(); i++) {
            this.container.removeItem(i, container.getItem(i).getCount());
        }
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

    public ItemStack getItem(int slot) {
        if (getContainerSize() < (slot + 1)) {
            return ItemStack.EMPTY;
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

    public void addButtonList(ButtonList buttonList) {
        this.buttonLists.add(buttonList);
    }

    private void checkButton() {
        for (Pair<Integer, Button> button : buttons) {
            button.getSecond().checkButton(this.container, button.getFirst());
        }
    }
}

