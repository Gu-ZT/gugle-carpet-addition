package dev.dubhe.gugle.carpet.tools.player;

import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

//#if MC>=12100
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
//#else
//$$ import net.minecraft.nbt.CompoundTag;
//$$ import net.minecraft.nbt.Tag;
//$$ import net.minecraft.nbt.ListTag;
//$$ import java.util.Iterator;
//#endif

public class FakePlayerAutoReplenishment {

    public static void autoReplenishment(@NotNull Player fakePlayer) {
        NonNullList<ItemStack> itemStackList = InventoryUtil.getItems(fakePlayer);
        replenishment(fakePlayer.getMainHandItem(), itemStackList);
        replenishment(fakePlayer.getOffhandItem(), itemStackList);
    }

    private static void replenishment(@NotNull ItemStack itemStack, NonNullList<ItemStack> itemStackList) {
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
            } else if (GcaSetting.fakePlayerAutoReplenishmentFormShulkerBox && hasContainer(eachItem)) {
                int result = pickItemFromBox(eachItem, itemStack, half);
                if (result == 0) {
                    continue;
                }
                itemStack.grow(result);
                return;
            }
        }
    }

    private static boolean hasContainer(@NotNull ItemStack stack) {
        //#if MC>=12100
        return stack.has(DataComponents.CONTAINER);
        //#else
        //$$ CompoundTag tag = stack.getTag();
        //$$ if (tag == null) return false;
        //$$ return tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("Items", Tag.TAG_LIST);
        //#endif
    }

    // 从潜影盒拿取物品，请注意：在创造模式下使用鼠标中键复制物品（不是指选取方块）时，物品组件仅被浅拷贝。
    private static int pickItemFromBox(@NotNull ItemStack shulkerBox, ItemStack itemStack, int count) {
        //#if MC>=12100
        ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
        if (contents == null) return 0;
        // 潜影盒没有容器组件
        for (ItemStack stack : contents.nonEmptyItems()) {
            if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                int temp;
                if (stack.getCount() >= count) {
                    stack.shrink(count);
                    temp = count;
                } else {
                    temp = stack.getCount();
                    stack.setCount(0);
                }
                ifIsEmptyClear(shulkerBox);
                return temp;
            }
        }
        //#else
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
        //$$     if (stack.getCount() > count) {
        //$$         temp = count;
        //$$         stack.shrink(count);
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
        //#endif
        return 0;
    }

    // 如果潜影盒为空，将物品栏组件替换为空以保证潜影盒堆叠的正常运行
    //#if MC>=12100
    private static void ifIsEmptyClear(@NotNull ItemStack shulkerBox) {
        ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
        if (contents == null) {
            return;
        }
        // 潜影盒中还有物品
        if (contents.nonEmptyItems().iterator().hasNext()) {
            return;
        }
        // 潜影盒中已经没有物品了
        shulkerBox.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    }
    //#else
    //#endif
}
