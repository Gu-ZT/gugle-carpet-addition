package dev.dubhe.gugle.carpet.tools;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FakePlayer {

    public static boolean checkButton(int slot, boolean flag, SimpleContainer container, int itemCount) {
        return checkButton(slot, flag, container, itemCount, Component.literal("on"), Component.literal("off"));
    }

    public static boolean checkButton(int slot, boolean flag, SimpleContainer container, int itemCount, Component on,
            Component off) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("GcaClear", true);
        ItemStack button_off = new ItemStack(Items.STRUCTURE_VOID, itemCount);
        ItemStack button_on = new ItemStack(Items.BARRIER, itemCount);
        button_off.setTag(compoundTag);
        button_off.setHoverName(off);
        button_on.setTag(compoundTag);
        button_on.setHoverName(on);
        ItemStack item = container.getItem(slot);
        if (item.isEmpty() && flag) {
            container.setItem(slot, button_off);
            button_on.setCount(0);
        } else if (item.isEmpty()) {
            container.setItem(slot, button_on);
            button_off.setCount(0);
        } else if (flag) {
            container.setItem(slot, button_on);
        } else {
            container.setItem(slot, button_off);
        }
        return container.getItem(slot).is(Items.BARRIER);
    }

    public static void syncItem(EntityPlayerMPFake fakePlayer, SimpleContainer container) {
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = fakePlayer.getInventory().getItem(i);
            if (itemStack == container.getItem(i + 9)) {
                container.setItem(i + 9, itemStack);
            } else {
                fakePlayer.getInventory().setItem(i, container.getItem(i + 9));
            }
        }
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = fakePlayer.getInventory().getItem(i);
            if (itemStack == container.getItem(i + 45)) {
                container.setItem(i + 45, itemStack);
            } else {
                fakePlayer.getInventory().setItem(i, container.getItem(i + 45));
            }
        }
        for (int i = 0; i < 4; i++) {
            ItemStack itemStack = fakePlayer.getInventory().getArmor(i);
            if (itemStack == container.getItem(4 - i)) {
                container.setItem(4 - i, itemStack);
            } else {
                fakePlayer.getInventory().armor.set(i, container.getItem(4 - i));
            }
        }
        ItemStack itemStack = fakePlayer.getOffhandItem();
        if (itemStack == container.getItem(7)) {
            container.setItem(7, itemStack);
        } else {
            fakePlayer.setItemSlot(EquipmentSlot.OFFHAND, container.getItem(7));
        }
    }
}
