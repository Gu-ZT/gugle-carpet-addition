package dev.dubhe.gugle.carpet.tools;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FakePlayerInventoryMenu extends ChestMenu {
    public FakePlayerInventoryMenu(int i, Inventory inventory, Container container) {
        super(MenuType.GENERIC_9x6, i, inventory, container, 6);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        ItemStack remainingItem = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            remainingItem = slotStack.copy();
            if (slotIndex < 54) {
                if (!this.moveItemStackTo(slotStack, 54, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.getItem() instanceof ArmorItem armorItem) {
                // 如果是盔甲，移动到盔甲槽
                int ordinal = armorItem.getType().ordinal();
                if (moveToArmor(slotStack, ordinal) || moveToInventory(slotStack)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.has(DataComponents.FOOD)) {
                // 如果是食物，移动到副手
                if (moveToOffHand(slotStack) || (moveToInventory(slotStack))) {
                    return ItemStack.EMPTY;
                }
            } else if (moveToInventory(slotStack)) {
                // 其它物品移动的物品栏中
                return ItemStack.EMPTY;
            }
            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return remainingItem;
    }

    // 移动到副手
    private boolean moveToOffHand(ItemStack slotStack) {
        return this.moveItemStackTo(slotStack, 7, 8, false);
    }

    // 移动到盔甲槽
    private boolean moveToArmor(ItemStack slotStack, int ordinal) {
        return this.moveItemStackTo(slotStack, ordinal + 1, ordinal + 2, false);
    }

    // 将物品移动的物品栏
    private boolean moveToInventory(ItemStack slotStack) {
        return !this.moveItemStackTo(slotStack, 18, 54, false);
    }
}
