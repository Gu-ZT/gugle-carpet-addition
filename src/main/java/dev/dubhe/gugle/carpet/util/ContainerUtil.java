package dev.dubhe.gugle.carpet.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ContainerUtil {
    public static boolean hasContainer(ItemStack stack) {
        return stack.has(DataComponents.CONTAINER);
    }

    // 从潜影盒拿取物品，请注意：在创造模式下使用鼠标中键复制物品（不是指选取方块）时，物品组件仅被浅拷贝。
    public static int pickItemFromShulker(ItemStack handItem, List<ItemStack> shulkerItems, int count) {
        for (ItemStack shulkerBox : shulkerItems) {
            ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
            // 空的潜影盒
            if (contents == null || contents == ItemContainerContents.EMPTY) continue;
            // 深拷贝
            List<ItemStack> list = contents
                //#if MC < 260000
                .stream()
                //#else
                //$$ .allItemsCopyStream()
                //#endif
                .collect(Collectors.toList());

            for (ItemStack stack : list) {
                if (!ItemStack.isSameItemSameComponents(handItem, stack)) continue;
                int picked;
                if (stack.getCount() >= count) {
                    stack.shrink(count);
                    picked = count;
                } else {
                    picked = stack.getCount();
                    stack.setCount(0);
                }
                shulkerBox.set(DataComponents.CONTAINER,
                    list.stream().allMatch(ItemStack::isEmpty) ?
                        ItemContainerContents.EMPTY :
                        ItemContainerContents.fromItems(list)
                );
                return picked;
            }
        }
        return 0;
    }

    public static boolean replenishmentTool(Player fakePlayer, EquipmentSlot slot, List<ItemStack> shulkerItems, Predicate<ItemStack> predicate) {
        for (ItemStack shulkerBox : shulkerItems) {
            ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
            // 空的潜影盒
            if (contents == null || contents == ItemContainerContents.EMPTY) continue;
            // 深拷贝
            List<ItemStack> list = contents
                //#if MC < 260000
                .stream()
                //#else
                //$$ .allItemsCopyStream()
                //#endif
                .collect(Collectors.toList());

            for (int index = 0; index < list.size(); index++) {
                ItemStack item = list.get(index);
                if (predicate.test(item)) {
                    ItemStack itemBySlot = fakePlayer.getItemBySlot(slot);
                    ItemStack copy = item.copy();
                    list.set(index, itemBySlot);
                    fakePlayer.setItemSlot(slot, copy);
                    ItemContainerContents container = list.stream().allMatch(ItemStack::isEmpty) ?
                        ItemContainerContents.EMPTY :
                        ItemContainerContents.fromItems(list);
                    shulkerBox.set(DataComponents.CONTAINER, container);
                    return true;
                }
            }
        }

        return false;
    }

}
