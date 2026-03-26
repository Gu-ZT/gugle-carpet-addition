package dev.dubhe.gugle.carpet.tools.player;

import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.util.ContainerUtil;
import dev.dubhe.gugle.carpet.util.InventoryUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
//#if MC < 12005
//$$ import java.util.Optional;
//#endif

public class FakePlayerAutoReplaceTool {
    @SuppressWarnings("UnnecessaryReturnStatement")
    //#if MC>=12005
    public static void autoReplaceTool(Player fakePlayer, Item item, EquipmentSlot equipmentSlot) {
        ItemStack itemStack = fakePlayer.getItemBySlot(equipmentSlot);
        //#else
        //$$ public static void autoReplaceTool(Player fakePlayer, Item item, ItemStack itemStack) {
        //#endif
        if (itemStack.isEmpty() || (
            itemStack.isDamageableItem() &&
            ("keep".equals(GcaSetting.fakePlayerAutoReplaceTool) || InventoryUtil.hasMendingEnchant(itemStack)) &&
            itemStack.getMaxDamage() - itemStack.getDamageValue() <= 10
        )) {
            //#if MC < 12005
            //$$ Optional<EquipmentSlot> optional = InventoryUtil.getEquipmentSlot(fakePlayer, itemStack);
            //$$ if (optional.isEmpty()) {
            //$$     return;
            //$$ }
            //$$ EquipmentSlot equipmentSlot = optional.get();
            //#endif
            Predicate<ItemStack> predicate = itemReplacePredicate(item);
            boolean replaced = replaceTool(equipmentSlot, predicate, fakePlayer);
            if (replaced) {
                return;
            }
            // 没有可供切换的工具，切换到无耐久的物品以避免经验修补工具损坏
            // - 左键挖掘方块时切换到无耐久物品通常不会有什么影响
            // - 但是右键交互方块时（例如使用锄头锄地）切换物品可能会出现问题，例如乱放置方块或乱使用物品
            // - 或许可以在没有合适的工具时自动停止右键？
            // replaceTool(equipmentSlot, stack -> !stack.isDamageableItem(), fakePlayer);
        }
    }

    public static boolean replaceTool(EquipmentSlot slot, Predicate<ItemStack> predicate, Player fakePlayer) {
        if (predicate.test(fakePlayer.getItemBySlot(slot))) {
            return true;
        }
        Inventory inventory = fakePlayer.getInventory();
        List<ItemStack> shulkerItems = new ArrayList<>(inventory.getContainerSize());
        // 从背包补货
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.isEmpty()) {
                continue;
            }
            if (predicate.test(itemStack)) {
                ItemStack itemBySlot = fakePlayer.getItemBySlot(slot);
                ItemStack copy = itemStack.copy();
                inventory.setItem(i, itemBySlot);
                fakePlayer.setItemSlot(slot, copy);
                return true;
            } else if (GcaSetting.fakePlayerAutoReplenishmentFormShulkerBox && ContainerUtil.hasContainer(itemStack)) {
                shulkerItems.add(itemStack);
            }
        }
        // 从潜影盒补货
        return ContainerUtil.replenishmentTool(fakePlayer, slot, shulkerItems, predicate);
    }

    private static Predicate<ItemStack> itemReplacePredicate(Item item) {
        boolean keepTool = "keep".equals(GcaSetting.fakePlayerAutoReplaceTool);
        return itemStack -> itemStack.getItem().getClass() == item.getClass() &&
            ((!keepTool && !InventoryUtil.hasMendingEnchant(itemStack)) ||
                itemStack.getMaxDamage() - itemStack.getDamageValue() > 10);
    }
}
