package dev.dubhe.gugle.carpet.api.menu.control;

import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

//#if MC>=12005
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//#endif

public class Button {
    public static final String GCA_CLEAR = "GcaClear";
    private final ItemStack onItem;
    private final ItemStack offItem;
    private final List<Runnable> turnOnRunnableList = new ArrayList<>();
    private final List<Runnable> turnOffRunnableList = new ArrayList<>();
    CompoundTag compoundTag = new CompoundTag();
    private boolean init = false;
    private boolean flag;

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
        this(
            defaultState, Items.BARRIER, Items.STRUCTURE_VOID, 1,
            ComponentHelper.tr(
                key,
                Color.GREEN,
                Style.EMPTY.withBold(true).withItalic(false),
                ComponentHelper.tr("gca.button.on")
            ),
            ComponentHelper.tr(
                key,
                Color.RED,
                Style.EMPTY.withBold(true).withItalic(false),
                ComponentHelper.tr("gca.button.off")
            )
        );
    }

    public Button(boolean defaultState, Item onItem, Item offItem) {
        this(defaultState, onItem, offItem, 1);
    }

    public Button(boolean defaultState, Item onItem, Item offItem, int itemCount) {
        this(
            defaultState, onItem, offItem, itemCount,
            ComponentHelper.tr("gca.button.on", Color.GREEN, Style.EMPTY.withBold(true).withItalic(false)),
            ComponentHelper.tr("gca.button.off", Color.RED, Style.EMPTY.withBold(true).withItalic(false))
        );
    }

    public Button(boolean defaultState, Item onItem, Item offItem, int itemCount, Component onText, Component offText) {
        this.flag = defaultState;
        this.compoundTag.putBoolean(GCA_CLEAR, true);

        ItemStack onItemStack = new ItemStack(onItem, itemCount);
        ItemStack offItemStack = new ItemStack(offItem, itemCount);

        //#if MC>=12005
        onItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
        onItemStack.set(DataComponents.ITEM_NAME, onText);
        offItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag.copy()));
        offItemStack.set(DataComponents.ITEM_NAME, offText);
        //#else
        //$$ onItemStack.setTag(compoundTag.copy());
        //$$ onItemStack.setHoverName(onText);
        //$$ offItemStack.setTag(compoundTag.copy());
        //$$ offItemStack.setHoverName(offText);
        //#endif

        this.onItem = onItemStack;
        this.offItem = offItemStack;
    }

    public Button(boolean defaultState, ItemStack onItem, ItemStack offItem) {
        this.flag = defaultState;
        this.compoundTag.putBoolean(GCA_CLEAR, true);

        ItemStack onItemStack = onItem.copy();
        ItemStack offItemStack = offItem.copy();

        //#if MC>=12005
        onItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag.copy()));
        offItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag.copy()));
        //#else
        //$$ onItemStack.setTag(compoundTag.copy());
        //$$ offItemStack.setTag(compoundTag.copy());
        //#endif

        this.onItem = onItemStack;
        this.offItem = offItemStack;
    }

    public boolean check(Container container, int slot) {
        if (!this.init) {
            updateButton(container, slot);
            this.init = true;
        }

        return container.getItem(slot).isEmpty();
    }

    public void execute(Container container, int slot) {
        this.flag = !flag;
        if (flag) {
            runTurnOnFunction();
        } else {
            runTurnOffFunction();
        }
        updateButton(container, slot);
    }

    public void checkButton(Container container, int slot) {
        if (!this.init) {
            updateButton(container, slot);
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

        updateButton(container, slot);
    }

    public void updateButton(Container container, int slot) {
        ItemStack onItemStack = this.onItem.copy();
        ItemStack offItemStack = this.offItem.copy();
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

    public void addTurnOnFunction(Runnable consumer) {
        this.turnOnRunnableList.add(consumer);
    }

    public void addTurnOffFunction(Runnable consumer) {
        this.turnOffRunnableList.add(consumer);
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
        for (Runnable turnOnConsumer : this.turnOnRunnableList) {
            turnOnConsumer.run();
        }
    }

    public void runTurnOffFunction() {
        for (Runnable turnOffConsumer : this.turnOffRunnableList) {
            turnOffConsumer.run();
        }
    }

    public boolean getFlag() {
        return flag;
    }
}
