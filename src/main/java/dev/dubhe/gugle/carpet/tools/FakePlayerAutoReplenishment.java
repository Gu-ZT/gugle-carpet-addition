package dev.dubhe.gugle.carpet.tools;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FakePlayerAutoReplenishment {

    public static void autoReplenishment(Player fakePlayer) {
        NonNullList<ItemStack> itemStackList = fakePlayer.getInventory().items;
        replenishment(fakePlayer.getMainHandItem(), itemStackList);
        replenishment(fakePlayer.getOffhandItem(), itemStackList);
    }

    public static void replenishment(ItemStack itemStack, NonNullList<ItemStack> itemStackList) {
        int count = itemStack.getMaxStackSize() / 2;
        if (itemStack.getCount() <= 8 && count > 8) {
            for (ItemStack itemStack1 : itemStackList) {
                if (itemStack1 == ItemStack.EMPTY || itemStack1 == itemStack) continue;
                if (ItemStack.isSameItemSameTags(itemStack1, itemStack)) {
                    if (itemStack1.getCount() > count) {
                        itemStack.setCount(itemStack.getCount() + count);
                        itemStack1.setCount(itemStack1.getCount() - count);
                    } else {
                        itemStack.setCount(itemStack.getCount() + itemStack1.getCount());
                        itemStack1.setCount(0);
                    }
                    break;
                }
            }
        }
    }

}
