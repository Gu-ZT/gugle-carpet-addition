package dev.dubhe.gugle.carpet.tools.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Button {

    private boolean flag;
    private final ItemStack onItem;
    private final ItemStack offItem;

    private List<BiConsumer<Container, Integer>> turnOn = new ArrayList<>();

    private List<BiConsumer<Container, Integer>> turnOff = new ArrayList<>();

    public Button() {
        this(Items.BARREL, Items.STRUCTURE_VOID);
    }

    public Button(int itemCount) {
        this(Items.BARREL, Items.STRUCTURE_VOID, itemCount);
    }

    public Button(Item onItem, Item offItem) {
        this(onItem, offItem, 1);
    }

    public Button(Item onItem, Item offItem, int itemCount) {
        this(onItem, offItem, itemCount, Component.literal("on"), Component.literal("off"));
    }

    public Button(Item onItem, Item offItem, int itemCount, Component on, Component off) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("GcaClear", true);
        this.onItem = new ItemStack(onItem, itemCount);
        this.onItem.setTag(compoundTag);
        this.onItem.setHoverName(on);
        this.offItem = new ItemStack(offItem, itemCount);
        this.offItem.setTag(compoundTag);
        this.offItem.setHoverName(off);
        this.flag = false;
    }

    public void checkButton(Container container, int slot) {
        ItemStack item = container.getItem(slot);
        if (item.isEmpty() && flag) {
            container.setItem(slot, offItem);
            this.flag = !flag;
            for(BiConsumer<Container, Integer> i : this.turnOn){
                i.accept(container, slot);
            }
        } else if (item.isEmpty()) {
            container.setItem(slot, onItem);
            this.flag = !flag;
            for(BiConsumer<Container, Integer> i : this.turnOff){
                i.accept(container, slot);
            }
        } else if (flag) {
            container.setItem(slot, onItem);
        } else {
            container.setItem(slot, offItem);
        }
    }

    public void addTurnOnFunction(BiConsumer<Container, Integer> func){
        this.turnOn.add(func);
    }

    public void addTurnOffFunction(BiConsumer<Container, Integer> func){
        this.turnOff.add(func);
    }

    public void turnOn() {
        this.flag = true;
    }

    public void turnOff() {
        this.flag = false;
    }
}
