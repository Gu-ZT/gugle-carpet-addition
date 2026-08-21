package dev.dubhe.gugle.carpet.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
//#if MC >= 260300
//$$ import net.minecraft.util.Prediction;
//#endif

public class ContainerUtil {
    public static boolean hasContainer(ItemStack stack) {
        return stack.has(DataComponents.CONTAINER);
    }

    // 从潜影盒拿取物品，请注意：在创造模式下使用鼠标中键复制物品（不是指选取方块）时，物品组件仅被浅拷贝。
    public static int pickItemFromShulker(Player player, ItemStack handItem, List<ItemStack> shulkerItems, int count) {
        for (ItemStack shulkerBox : shulkerItems) {
            ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
            // 空的潜影盒
            if (contents == null || contents == ItemContainerContents.EMPTY) continue;
            // 深拷贝
            List<ItemStack> list = contents
                //#if MC < 260000
                .stream()
                //#elseif MC < 260300
                //$$ .allItemsCopyStream()
                //#else
                //$$ .itemCopies()
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

                updateShulkerBoxContainer(player, shulkerBox, list);
                return picked;
            }
        }
        return 0;
    }

    public static boolean replenishmentTool(Player player, EquipmentSlot slot, List<ItemStack> shulkerItems, Predicate<ItemStack> predicate) {
        for (ItemStack shulkerBox : shulkerItems) {
            ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
            // 空的潜影盒
            if (contents == null || contents == ItemContainerContents.EMPTY) continue;
            // 深拷贝
            List<ItemStack> list = contents
                //#if MC < 260000
                .stream()
                //#elseif MC < 260300
                //$$ .allItemsCopyStream()
                //#else
                //$$ .itemCopies()
                //#endif
                .collect(Collectors.toList());

            for (int index = 0; index < list.size(); index++) {
                ItemStack item = list.get(index);
                if (predicate.test(item)) {
                    ItemStack itemBySlot = player.getItemBySlot(slot);
                    ItemStack copy = item.copy();
                    list.set(index, itemBySlot);
                    player.setItemSlot(slot, copy);
                    updateShulkerBoxContainer(player, shulkerBox, list);
                    return true;
                }
            }
        }

        return false;
    }

    private static void updateShulkerBoxContainer(Player player, ItemStack shulkerBox, List<ItemStack> items) {
        ItemContainerContents container = items.stream().allMatch(ItemStack::isEmpty) ?
            ItemContainerContents.EMPTY :
            ItemContainerContents.fromItems(items);

        if (shulkerBox.getCount() == 1) {
            shulkerBox.set(DataComponents.CONTAINER, container);
            return;
        }

        if (shulkerBox.getCount() > 1) {
            ItemStack newShulker = shulkerBox.copyWithCount(1);
            shulkerBox.shrink(1);
            newShulker.set(DataComponents.CONTAINER, container);
            if (!player.addItem(newShulker)) {
                ItemEntity itemEntity = player.drop(newShulker, false
                    //#if MC >= 260300
                    //$$ , Prediction.PREDICTED
                    //#endif
                );
                if (itemEntity != null) {
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setTarget(player.getUUID());
                }
            }
        }
    }

}
