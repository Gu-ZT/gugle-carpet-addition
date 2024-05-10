package dev.dubhe.gugle.carpet.tools;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class FakePlayerAutoReplenishment {

    public static void autoReplenishment(@NotNull Player fakePlayer) {
        NonNullList<ItemStack> itemStackList = fakePlayer.getInventory().items;
        replenishment(fakePlayer.getMainHandItem(), itemStackList);
        replenishment(fakePlayer.getOffhandItem(), itemStackList);
    }

    public static void replenishment(@NotNull ItemStack itemStack, NonNullList<ItemStack> itemStackList) {
        int rs = itemStack.getMaxStackSize() / 8;
        if (itemStack.isEmpty()) return;
        if (itemStack.getCount() > rs) return;
        int count = itemStack.getMaxStackSize() / 2;
        if (count <= rs) return;
        global:
        for (ItemStack itemStack1 : itemStackList) {
            if (itemStack1 == ItemStack.EMPTY || itemStack1 == itemStack) continue;
            if (ItemStack.isSameItemSameTags(itemStack1, itemStack)) {
                if (itemStack1.getCount() > count) {
                    itemStack.setCount(itemStack.getCount() + count);
                    itemStack1.setCount(itemStack1.getCount() - count);
                } else {
                    itemStack.setCount(itemStack.getCount() + itemStack1.getCount());
                    itemStack1.setCount(0);
                }
                break;
            } else if (itemStack1.is(Items.SHULKER_BOX) && itemStack1.hasTag()) {
                CompoundTag nbt = itemStack1.getTagElement("BlockEntityTag");
                if (nbt != null && nbt.contains("Items", Tag.TAG_LIST)) {
                    ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
                    Iterator<Tag> iterator = tagList.iterator();
                    int index = -1;
                    while (iterator.hasNext()) {
                        index += 1;
                        Tag next = iterator.next();
                        CompoundTag tag = next.getId() == 10 ? (CompoundTag) next : new CompoundTag();
                        ItemStack stack = ItemStack.of(tag);
                        if (!ItemStack.isSameItemSameTags(stack, itemStack)) continue;
                        if (stack.getCount() > count) {
                            itemStack.setCount(itemStack.getCount() + count);
                            stack.setCount(stack.getCount() - count);
                        } else {
                            itemStack.setCount(itemStack.getCount() + stack.getCount());
                            stack.setCount(0);
                        }
                        if (!stack.isEmpty()) {
                            CompoundTag newTag = stack.save(new CompoundTag());
                            newTag.putByte("Slot", tag.getByte("Slot"));
                            tagList.set(index, newTag);
                        } else iterator.remove();
                        break global;
                    }
                }
            }
        }
    }
}
