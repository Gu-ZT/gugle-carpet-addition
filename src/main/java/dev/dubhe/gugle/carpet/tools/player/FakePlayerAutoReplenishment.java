package dev.dubhe.gugle.carpet.tools.player;

import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.util.ContainerUtil;
import dev.dubhe.gugle.carpet.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FakePlayerAutoReplenishment {

    public static void autoReplenishment(ServerPlayer fakePlayer, InteractionHand hand) {
        ItemStack handItem = fakePlayer.getItemInHand(hand);
        int base = handItem.getMaxStackSize() / 8;
        if (handItem.isEmpty() || (handItem.getCount() > base)) return;
        int half = handItem.getMaxStackSize() / 2;
        if (half <= base) return;
        NonNullList<ItemStack> itemStackList = InventoryUtil.getItems(fakePlayer);
        if (!replenishment(fakePlayer, handItem, itemStackList, half) && GcaSetting.fakePlayerToolDamagedNotification) {
            FakePlayerNotification.sendRestockFailed(fakePlayer, handItem);
        }
    }

    private static boolean replenishment(ServerPlayer player, ItemStack handItem, NonNullList<ItemStack> backpackItems, int count) {
        List<ItemStack> shulkerItems = new ArrayList<>(backpackItems.size());
        for (ItemStack eachItem : backpackItems) {
            if (eachItem.isEmpty() || (eachItem == handItem)) {
                continue;
            }
            if (ItemStack.isSameItemSameComponents(eachItem, handItem)) {
                if (eachItem.getCount() > count) {
                    eachItem.shrink(count);
                } else {
                    count = eachItem.getCount();
                    eachItem.setCount(0);
                }
                handItem.grow(count);
                return true;
            } else if (GcaSetting.fakePlayerAutoReplenishmentFormShulkerBox && ContainerUtil.hasContainer(eachItem)) {
                shulkerItems.add(eachItem);
            }
        }
        int picked = ContainerUtil.pickItemFromShulker(player, handItem, shulkerItems, count);
        if (picked > 0) {
            handItem.grow(picked);
            return true;
        }
        return false;
    }
}
