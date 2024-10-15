package dev.dubhe.gugle.carpet.tools;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

public class FakePlayerAutoReplenishment {

    public static void autoReplenishment(@NotNull Player fakePlayer) {
        NonNullList<ItemStack> itemStackList = fakePlayer.getInventory().items;
        replenishment(fakePlayer.getMainHandItem(), itemStackList);
        replenishment(fakePlayer.getOffhandItem(), itemStackList);
    }

    private static void replenishment(@NotNull ItemStack itemStack, NonNullList<ItemStack> itemStackList) {
        int base = itemStack.getMaxStackSize() / 8;
        if (itemStack.isEmpty() || (itemStack.getCount() > base)) {
            return;
        }
        int half = itemStack.getMaxStackSize() / 2;
        if (half <= base) {
            return;
        }
        for (ItemStack eachItem : itemStackList) {
            if (eachItem.isEmpty() || (eachItem == itemStack)) {
                continue;
            }
            if (ItemStack.isSameItemSameComponents(eachItem, itemStack)) {
                if (eachItem.getCount() > half) {
                    itemStack.setCount(itemStack.getCount() + half);
                    eachItem.setCount(eachItem.getCount() - half);
                } else {
                    itemStack.setCount(itemStack.getCount() + eachItem.getCount());
                    eachItem.setCount(0);
                }
                break;
            } else if (eachItem.has(DataComponents.CONTAINER)) {
                int result = pickItemFromBox(eachItem, itemStack, half);
                if (result == 0) {
                    continue;
                }
                itemStack.grow(result);
                return;
            }
        }
    }

    // 从潜影盒拿取物品，请注意：在创造模式下使用鼠标中键复制物品（不是指选取方块）时，物品组件仅被浅拷贝。
    private static int pickItemFromBox(ItemStack shulkerBox, ItemStack itemStack, int count) {
        ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
        // 潜影盒没有容器组件
        if (contents == null) {
            return 0;
        }
        for (ItemStack stack : contents.nonEmptyItems()) {
            if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                int temp;
                if (stack.getCount() >= count) {
                    stack.shrink(count);
                    temp = count;
                } else {
                    temp = stack.getCount();
                    stack.setCount(0);
                }
                ifIsEmptyClear(shulkerBox);
                return temp;
            }
        }
        return 0;
    }

    // 如果潜影盒为空，将物品栏组件替换为空以保证潜影盒堆叠的正常运行
    private static void ifIsEmptyClear(ItemStack shulkerBox) {
        ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
        if (contents == null) {
            return;
        }
        // 潜影盒中还有物品
        if (contents.nonEmptyItems().iterator().hasNext()) {
            return;
        }
        // 潜影盒中已经没有物品了
        shulkerBox.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    }
}
