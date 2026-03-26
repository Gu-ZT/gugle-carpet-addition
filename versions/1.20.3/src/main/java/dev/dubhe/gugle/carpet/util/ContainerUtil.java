package dev.dubhe.gugle.carpet.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class ContainerUtil {
    public static boolean hasContainer(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return false;
        return tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("Items", Tag.TAG_LIST);
    }

    public static int pickItemFromShulker(ItemStack handItem, List<ItemStack> shulkerItems, int count) {
        for (ItemStack shulkerBox : shulkerItems) {
            CompoundTag nbt = shulkerBox.getTagElement("BlockEntityTag");
            if (nbt == null || !nbt.contains("Items", Tag.TAG_LIST)) return 0;
            ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
            Iterator<Tag> iterator = tagList.iterator();
            int index = -1;
            while (iterator.hasNext()) {
                index += 1;
                Tag next = iterator.next();
                CompoundTag tag = next.getId() == 10 ? (CompoundTag) next : new CompoundTag();
                ItemStack stack = ItemStack.of(tag);
                if (!ItemStack.isSameItemSameTags(handItem, stack)) continue;
                int picked;
                if (stack.getCount() > count) {
                    picked = count;
                    stack.shrink(count);
                } else {
                    picked = stack.getCount();
                    stack.setCount(0);
                }
                if (!stack.isEmpty()) {
                    CompoundTag newTag = stack.save(new CompoundTag());
                    newTag.putByte("Slot", tag.getByte("Slot"));
                    tagList.set(index, newTag);
                } else iterator.remove();
                return picked;
            }
        }
        return 0;
    }

    public static boolean replenishmentTool(Player fakePlayer, EquipmentSlot slot, List<ItemStack> shulkerItems, Predicate<ItemStack> predicate) {
        for (ItemStack shulkerBox : shulkerItems) {
            CompoundTag nbt = shulkerBox.getTagElement("BlockEntityTag");
            if (nbt == null) continue;
            ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
            for (int index = 0; index < tagList.size(); index++) {
                CompoundTag tag = tagList.getCompound(index);
                ItemStack stack = ItemStack.of(tag);
                if (!predicate.test(stack)) continue;
                ItemStack itemBySlot = fakePlayer.getItemBySlot(slot);
                ItemStack copy = stack.copy();
                fakePlayer.setItemSlot(slot, copy);
                if (itemBySlot.isEmpty()) {
                    tagList.remove(index);
                    if (tagList.isEmpty()) nbt.remove("Items");
                    return true;
                }
                CompoundTag newTag = itemBySlot.save(new CompoundTag());
                newTag.putByte("Slot", tag.getByte("Slot"));
                tagList.set(index, newTag);
                return true;
            }
        }
        return false;
    }

}
