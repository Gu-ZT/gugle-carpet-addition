package dev.dubhe.gugle.carpet.tools.player;

import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.util.ContainerUtil;
import dev.dubhe.gugle.carpet.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


//#if MC>=12005
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
//#else
//$$ import net.minecraft.nbt.CompoundTag;
//$$ import net.minecraft.nbt.Tag;
//$$ import net.minecraft.nbt.ListTag;
//$$ import java.util.Iterator;
//#endif

public class FakePlayerAutoReplenishment {

    public static void autoReplenishment(Player fakePlayer, InteractionHand hand) {
        NonNullList<ItemStack> itemStackList = InventoryUtil.getItems(fakePlayer);
        replenishment(fakePlayer.getItemInHand(hand), itemStackList);
    }

    private static void replenishment(ItemStack itemStack, NonNullList<ItemStack> itemStackList) {
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
            } else if (GcaSetting.fakePlayerAutoReplenishmentFormShulkerBox && ContainerUtil.hasContainer(eachItem)) {
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
    private static int pickItemFromBox(ItemStack shulkerBox, ItemStack itemStack, int requestCount) {
        //#if MC>=12005
        ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
        // 空的潜影盒
        if (contents == null || contents == ItemContainerContents.EMPTY) return 0;
        // 深拷贝
        List<ItemStack> list = contents
            //#if MC < 260000
            .stream()
            //#else
            //$$ .allItemsCopyStream()
            //#endif
            .toList();
        int count = 0;

        for (ItemStack stack : list) {
            if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                if (stack.getCount() >= requestCount) {
                    stack.shrink(requestCount);
                    count = requestCount;
                } else {
                    count = stack.getCount();
                    stack.setCount(0);
                }
                break;
            }
        }

        if (count > 0) {
            // 将深拷贝的组件写回潜影盒
            shulkerBox.set(DataComponents.CONTAINER,
                list.stream().allMatch(ItemStack::isEmpty) ?
                    ItemContainerContents.EMPTY :
                    ItemContainerContents.fromItems(list)
            );
        }

        return count;
        //#else
        //$$ CompoundTag nbt = shulkerBox.getTagElement("BlockEntityTag");
        //$$ if (nbt == null || !nbt.contains("Items", Tag.TAG_LIST)) return 0;
        //$$ ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        //$$ Iterator<Tag> iterator = tagList.iterator();
        //$$ int index = -1;
        //$$ int temp;
        //$$ while (iterator.hasNext()) {
        //$$     index += 1;
        //$$     Tag next = iterator.next();
        //$$     CompoundTag tag = next.getId() == 10 ? (CompoundTag) next : new CompoundTag();
        //$$     ItemStack stack = ItemStack.of(tag);
        //$$     if (!ItemStack.isSameItemSameTags(stack, itemStack)) continue;
        //$$     if (stack.getCount() > requestCount) {
        //$$         temp = requestCount;
        //$$         stack.shrink(requestCount);
        //$$     } else {
        //$$         temp = stack.getCount();
        //$$         stack.setCount(0);
        //$$     }
        //$$     if (!stack.isEmpty()) {
        //$$         CompoundTag newTag = stack.save(new CompoundTag());
        //$$         newTag.putByte("Slot", tag.getByte("Slot"));
        //$$         tagList.set(index, newTag);
        //$$     } else iterator.remove();
        //$$     return temp;
        //$$ }
        //$$ return 0;
        //#endif
    }

}
