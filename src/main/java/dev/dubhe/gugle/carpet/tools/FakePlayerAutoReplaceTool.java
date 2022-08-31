package dev.dubhe.gugle.carpet.tools;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FakePlayerAutoReplaceTool {

    public static void autoReplaceTool(Player fakePlayer) {
        ItemStack mainHand = fakePlayer.getMainHandItem();
        ItemStack offHand = fakePlayer.getOffhandItem();
        if (!mainHand.isEmpty() && (mainHand.getMaxDamage() - mainHand.getDamageValue()) <= 10)
            replaceTool(EquipmentSlot.MAINHAND, fakePlayer);
        if (!offHand.isEmpty() && (offHand.getMaxDamage() - offHand.getDamageValue()) <= 10)
            replaceTool(EquipmentSlot.OFFHAND, fakePlayer);
    }

    public static void replaceTool(EquipmentSlot slot, Player fakePlayer) {
        ItemStack itemStack = fakePlayer.getItemBySlot(slot);
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack1 = fakePlayer.getInventory().getItem(i);
            if (itemStack1 == ItemStack.EMPTY || itemStack1 == itemStack) continue;
            if (itemStack1.getItem().getClass() == itemStack.getItem().getClass() && (itemStack1.getMaxDamage() - itemStack1.getDamageValue()) > 10) {
                ItemStack itemStack2 = itemStack1.copy();
                fakePlayer.getInventory().setItem(i, itemStack);
                fakePlayer.setItemSlot(slot, itemStack2);
                break;
            }
        }
    }
}
