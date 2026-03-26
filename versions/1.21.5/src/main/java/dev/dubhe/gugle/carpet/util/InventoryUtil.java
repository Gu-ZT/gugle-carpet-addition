package dev.dubhe.gugle.carpet.util;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;


public class InventoryUtil {
    public static NonNullList<ItemStack> getItems(Player player) {
        return player.getInventory().getNonEquipmentItems();
    }

    public static NonNullList<ItemStack> getArmor(Player player) {
        return new NonNullList<>(
            List.of(
                player.getItemBySlot(EquipmentSlot.FEET),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.HEAD)
            ),
            ItemStack.EMPTY
        ) {
            @Override
            public ItemStack get(int index) {
                return switch (index) {
                    case 3 -> player.getItemBySlot(EquipmentSlot.HEAD);
                    case 2 -> player.getItemBySlot(EquipmentSlot.CHEST);
                    case 1 -> player.getItemBySlot(EquipmentSlot.LEGS);
                    case 0 -> player.getItemBySlot(EquipmentSlot.FEET);
                    default -> ItemStack.EMPTY;
                };
            }

            @Override
            public ItemStack set(int index, ItemStack stack) {
                switch (index) {
                    case 3 -> player.setItemSlot(EquipmentSlot.HEAD, stack);
                    case 2 -> player.setItemSlot(EquipmentSlot.CHEST, stack);
                    case 1 -> player.setItemSlot(EquipmentSlot.LEGS, stack);
                    case 0 -> player.setItemSlot(EquipmentSlot.FEET, stack);
                }
                return stack;
            }
        };
    }

    public static NonNullList<ItemStack> getOffHand(Player player) {
        return new NonNullList<>(
            List.of(player.getItemBySlot(EquipmentSlot.OFFHAND)),
            ItemStack.EMPTY
        ) {
            @Override
            public ItemStack get(int index) {
                return index == 0 ? player.getItemBySlot(EquipmentSlot.OFFHAND) : ItemStack.EMPTY;
            }

            @Override
            public ItemStack set(int index, ItemStack stack) {
                if (index == 0) {
                    player.setItemSlot(EquipmentSlot.OFFHAND, stack);
                }
                return stack;
            }
        };
    }

    public static int getSelected(Player player) {
        return player.getInventory().getSelectedSlot();
    }

    public static boolean hasMendingEnchant(ItemStack itemStack) {
        ItemEnchantments enchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> key : enchantments.keySet()) {
            if (key.value().effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP)) {
                return true;
            }
        }
        return false;
    }
}
