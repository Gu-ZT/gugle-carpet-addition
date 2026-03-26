package dev.dubhe.gugle.carpet.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

//#if MC >= 12100
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Holder;
//#else
//$$ import net.minecraft.world.item.enchantment.EnchantmentHelper;
//$$ import net.minecraft.world.item.enchantment.Enchantments;
//#endif
//#if MC < 12005
//$$ import net.minecraft.world.entity.EquipmentSlot;
//$$ import java.util.Optional;
//#endif

public class InventoryUtil {
    public static NonNullList<ItemStack> getItems(Player player) {
        return player.getInventory().items;
    }

    public static NonNullList<ItemStack> getArmor(Player player) {
        return player.getInventory().armor;
    }

    public static NonNullList<ItemStack> getOffHand(Player player) {
        return player.getInventory().offhand;
    }

    public static int getSelected(Player player) {
        return player.getInventory().selected;
    }

    public static boolean hasMendingEnchant(ItemStack itemStack) {
        //#if MC>=12100
        ItemEnchantments enchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> key : enchantments.keySet()) {
            if (key.value().effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP)) {
                return true;
            }
        }
        return false;
        //#else
        //$$ return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, itemStack) > 0;
        //#endif
    }

    //#if MC<12005
    //$$ public static Optional<EquipmentSlot> getEquipmentSlot(Player fakePlayer, ItemStack itemStack) {
    //$$     for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
    //$$         if (fakePlayer.getItemBySlot(equipmentSlot) == itemStack) {
    //$$             return Optional.of(equipmentSlot);
    //$$         }
    //$$     }
    //$$     return Optional.empty();
    //$$ }
    //#endif
}
