package dev.dubhe.gugle.carpet.tools;

import dev.dubhe.gugle.carpet.api.menu.CustomMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class FakePlayerContainer extends CustomMenu {
    protected final Player player;

    public FakePlayerContainer(Player player) {
        this.player = player;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        Map.Entry<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        if (pair != null) {
            return pair.getKey().get(pair.getValue());
        } else {
            return ItemStack.EMPTY;
        }
    }

    public abstract Map.Entry<NonNullList<ItemStack>, Integer> getItemSlot(int slot);

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        Map.Entry<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getKey();
            slot = pair.getValue();
        }
        if (list != null && !list.get(slot).isEmpty()) {
            return ContainerHelper.removeItem(list, slot, amount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        Map.Entry<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getKey();
            slot = pair.getValue();
        }
        if (list != null && !list.get(slot).isEmpty()) {
            ItemStack itemStack = list.get(slot);
            list.set(slot, ItemStack.EMPTY);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        Map.Entry<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getKey();
            slot = pair.getValue();
        }
        if (list != null) {
            list.set(slot, stack);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.player.isAlive() && player.distanceToSqr(this.player) <= 64.0;
    }
}
