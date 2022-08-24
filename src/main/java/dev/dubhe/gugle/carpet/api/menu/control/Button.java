package dev.dubhe.gugle.carpet.api.menu.control;

import dev.dubhe.gugle.carpet.api.Function;
import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class Button {

    private boolean init = false;
    private boolean flag;
    private final Item onItem;
    private final Item offItem;
    private final int itemCount;
    private final Component onText;
    private final Component offText;
    CompoundTag compoundTag = new CompoundTag();

    private final List<Function> turnOnFunctions = new ArrayList<>();

    private final List<Function> turnOffFunctions = new ArrayList<>();

    public Button() {
        this(true, Items.BARRIER, Items.STRUCTURE_VOID);
    }

    public Button(boolean defaultState) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID);
    }

    public Button(boolean defaultState, int itemCount) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID, itemCount);
    }

    public Button(boolean defaultState, int itemCount, Component onText, Component offText) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID, itemCount, onText, offText);
    }

    public Button(boolean defaultState, Component onText, Component offText) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID, 1, onText, offText);
    }

    public Button(boolean defaultState, String key) {
        this(defaultState, Items.BARRIER, Items.STRUCTURE_VOID, 1,
                ComponentTranslate.trans(key, Color.GREEN, Style.EMPTY.withBold(true).withItalic(false), "on"),
                ComponentTranslate.trans(key, Color.RED, Style.EMPTY.withBold(true).withItalic(false), "off")
        );
    }

    public Button(boolean defaultState, Item onItem, Item offItem) {
        this(defaultState, onItem, offItem, 1);
    }

    public Button(boolean defaultState, Item onItem, Item offItem, int itemCount) {
        this(defaultState, onItem, offItem, itemCount,
                ComponentTranslate.trans("on", Color.GREEN, Style.EMPTY.withBold(true).withItalic(false)),
                ComponentTranslate.trans("off", Color.RED, Style.EMPTY.withBold(true).withItalic(false))
        );
    }

    public Button(boolean defaultState, Item onItem, Item offItem, int itemCount, Component onText, Component offText) {
        this.flag = defaultState;
        this.onText = onText;
        this.offText = offText;
        this.onItem = onItem;
        this.offItem = offItem;
        this.itemCount = itemCount;
        this.compoundTag.putBoolean("GcaClear", true);
    }

    public void checkButton(Container container, int slot) {
        ItemStack onItemStack = new ItemStack(this.onItem, this.itemCount);
        onItemStack.setTag(compoundTag.copy());
        onItemStack.setHoverName(this.onText);

        ItemStack offItemStack = new ItemStack(this.offItem, this.itemCount);
        offItemStack.setTag(compoundTag.copy());
        offItemStack.setHoverName(this.offText);

        if (!this.init) {
            updateButton(container, slot, onItemStack, offItemStack);
            this.init = true;
        }

        ItemStack item = container.getItem(slot);

        if (item.isEmpty()) {
            this.flag = !flag;
            if (flag) {
                runTurnOnFunction();
            } else {
                runTurnOffFunction();
            }
        }

        updateButton(container, slot, onItemStack, offItemStack);
    }

    public void updateButton(Container container, int slot, ItemStack onItemStack, ItemStack offItemStack) {
        if (!(
                container.getItem(slot).is(onItemStack.getItem()) ||
                container.getItem(slot).is(offItemStack.getItem()) ||
                container.getItem(slot).isEmpty()
        )) {
            return;
        }
        if (flag) {
            container.setItem(slot, onItemStack);
        } else {
            container.setItem(slot, offItemStack);
        }
    }

    public void addTurnOnFunction(Function function) {
        this.turnOnFunctions.add(function);
    }

    public void addTurnOffFunction(Function function) {
        this.turnOffFunctions.add(function);
    }

    public void turnOnWithoutFunction() {
        this.flag = true;
    }

    public void turnOffWithoutFunction() {
        this.flag = false;
    }

    public void turnOn() {
        this.flag = true;
        runTurnOnFunction();
    }

    public void turnOff() {
        this.flag = false;
        runTurnOffFunction();
    }

    public void runTurnOnFunction() {
        for (Function turnOnFunction : this.turnOnFunctions) {
            turnOnFunction.accept();
        }
    }

    public void runTurnOffFunction() {
        for (Function turnOffFunction : this.turnOffFunctions) {
            turnOffFunction.accept();
        }
    }

    public boolean getFlag() {
        return flag;
    }
}
