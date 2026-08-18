package dev.dubhe.gugle.carpet.api.menu.control;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

//#if MC>=12005
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//#endif

public class Button {
    public static final String GCA_CLEAR = "GcaClear";
    public static final String GCA_BUTTON = "GcaButton";
    private final Container container;
    private final int slot;
    private final ItemStack onItem;
    private final ItemStack offItem;
    private final List<Consumer<Button>> turnOnCallback;
    private final List<Consumer<Button>> turnOffCallback;
    private boolean status;

    public Button(
        Container container, int slot,
        ItemStack on, ItemStack off,
        List<Consumer<Button>> turnOnCallback,
        List<Consumer<Button>> turnOffCallback,
        boolean status
    ) {
        this.container = container;
        this.slot = slot;
        this.onItem = on;
        this.offItem = off;
        this.turnOnCallback = turnOnCallback;
        this.turnOffCallback = turnOffCallback;
        this.status = status;
        this.init();
    }

    private void init() {
        CompoundTag compound = new CompoundTag();
        compound.putBoolean(GCA_CLEAR, true);
        compound.putInt(GCA_BUTTON, this.slot);

        //#if MC>=12005
        this.onItem.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
        this.offItem.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
        //#else
        //$$ this.onItem.setTag(compound.copy());
        //$$ this.offItem.setTag(compound.copy());
        //#endif

        ItemStack slotItem = this.status ? this.onItem.copy() : this.offItem.copy();
        this.container.setItem(this.slot, slotItem);
    }

    public void refresh() {
        ItemStack slotItem = this.status ? this.onItem.copy() : this.offItem.copy();
        this.container.setItem(this.slot, slotItem);
    }

    public boolean getStatus() {
        return this.status;
    }

    public boolean clicked() {
        return this.container.getItem(this.slot).isEmpty();
    }

    public void clickCallback() {
        this.status = !this.status;
        this.callback();
    }

    public void changeStatus(boolean status) {
        this.changeStatus(status, false);
    }

    public void changeStatus(boolean status, boolean passExecute) {
        this.status = status;
        if (passExecute) return;
        this.callback();
    }

    public void callback() {
        List<Consumer<Button>> callbacks = this.status ? this.turnOnCallback : this.turnOffCallback;
        for (Consumer<Button> callback : callbacks) {
            callback.accept(this);
        }
    }
}
