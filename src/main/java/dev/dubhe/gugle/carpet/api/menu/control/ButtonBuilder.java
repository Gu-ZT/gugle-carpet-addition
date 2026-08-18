package dev.dubhe.gugle.carpet.api.menu.control;

import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//#if MC>=12005
import net.minecraft.core.component.DataComponents;
//#endif

public class ButtonBuilder {
    private static final Style STYLE = Style.EMPTY.withBold(true).withItalic(false);

    private ItemStack onItem = Items.BARRIER.getDefaultInstance();
    private ItemStack offItem = Items.STRUCTURE_VOID.getDefaultInstance();
    private Component onText;
    private Component offText;
    private final List<Consumer<Button>> turnOnRunnable = new ArrayList<>();
    private final List<Consumer<Button>> turnOffRunnable = new ArrayList<>();
    private boolean defaultState = false;
    @Nullable
    private Button button = null;

    public ButtonBuilder(Component onText, Component offText) {
        this.onText = onText;
        this.offText = offText;
    }

    public static ButtonBuilder ofKey(String key) {
        Component on = ComponentHelper.tr(key, Color.GREEN, STYLE, ComponentHelper.tr("gca.button.on"));
        Component off = ComponentHelper.tr(key, Color.RED, STYLE, ComponentHelper.tr("gca.button.off"));
        return new ButtonBuilder(on, off);
    }

    public static ButtonBuilder ofName(String name) {
        return ofComponent(Component.literal(name));
    }

    public static ButtonBuilder ofComponent(Component component) {
        return new ButtonBuilder(component, component);
    }

    public Button get() {
        if (this.button == null) {
            throw new IllegalStateException("The button has not been initialized yet.");
        }
        return this.button;
    }

    public Button build(Container container, int slot) {
        this.setText(this.onItem, this.onText);
        this.setText(this.offItem, this.offText);

        this.button = new Button(
            container,
            slot,
            this.onItem,
            this.offItem,
            List.copyOf(this.turnOnRunnable),
            List.copyOf(this.turnOffRunnable),
            this.defaultState
        );

        return this.button;
    }

    private void setText(ItemStack item, Component text) {
        //#if MC>=12005
        item.set(DataComponents.ITEM_NAME, text);
        //#else
        //$$ item.setHoverName(text);
        //#endif
    }

    public ButtonBuilder setOnItem(Item item) {
        return this.setOnItem(new ItemStack(item));
    }

    public ButtonBuilder setOnItem(Item item, int count) {
        return this.setOnItem(new ItemStack(item, count));
    }

    public ButtonBuilder setOffItem(Item item) {
        return this.setOffItem(new ItemStack(item));
    }

    public ButtonBuilder setOffItem(Item item, int count) {
        return this.setOffItem(new ItemStack(item, count));
    }

    public ButtonBuilder setOnItem(ItemStack item) {
        this.onItem = item;
        return this;
    }

    public ButtonBuilder setOffItem(ItemStack offItem) {
        this.offItem = offItem;
        return this;
    }

    public ButtonBuilder setItem(Item item) {
        return this.setItem(new ItemStack(item));
    }

    public ButtonBuilder setItem(ItemStack item) {
        return this.setOnItem(item).setOffItem(item);
    }

    public ButtonBuilder setOnItemCount(int count) {
        this.onItem.setCount(count);
        return this;
    }

    public ButtonBuilder setOffItemCount(int count) {
        this.offItem.setCount(count);
        return this;
    }

    public ButtonBuilder setItemCount(int count) {
        return this.setOnItemCount(count).setOffItemCount(count);
    }

    public ButtonBuilder setOnText(Component onText) {
        this.onText = onText;
        return this;
    }

    public ButtonBuilder setOffText(Component offText) {
        this.offText = offText;
        return this;
    }

    public ButtonBuilder setText(Component text) {
        return this.setOnText(text).setOffText(text);
    }

    public ButtonBuilder addTurnOnCallback(Consumer<Button> turnOnRunnable) {
        this.turnOnRunnable.add(turnOnRunnable);
        return this;
    }

    public ButtonBuilder addTurnOffCallback(Consumer<Button> turnOffRunnable) {
        this.turnOffRunnable.add(turnOffRunnable);
        return this;
    }

    public ButtonBuilder setDefaultState(boolean defaultState) {
        this.defaultState = defaultState;
        return this;
    }

    public ButtonBuilder resetButton() {
        return this.setDefaultState(false).addTurnOnCallback(it -> it.changeStatus(false, true));
    }

    public ButtonBuilder noneButton() {
        Component text = ComponentHelper.tr("gca.button.none", Color.WHITE, STYLE);

        return this.resetButton()
            .setText(text)
            .setItem(
                //#if MC < 260200
                Items.RED_STAINED_GLASS_PANE
                //#else
                //$$ Items.STAINED_GLASS_PANE.red()
                //#endif

            );
    }

}
