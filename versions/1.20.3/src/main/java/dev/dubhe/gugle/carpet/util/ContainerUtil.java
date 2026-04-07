package dev.dubhe.gugle.carpet.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class ContainerUtil {
    public static boolean hasContainer(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return false;
        return tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("Items", Tag.TAG_LIST);
    }

    public static int pickItemFromShulker(Player player, ItemStack handItem, List<ItemStack> shulkerItems, int count) {
        for (ItemStack shulkerBox : shulkerItems) {
            CompoundTag nbt = shulkerBox.getTagElement("BlockEntityTag");
            if (nbt == null || !nbt.contains("Items", Tag.TAG_LIST)) return 0;
            ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND).copy();

            for (int index = 0; index < tagList.size(); index++) {
                CompoundTag tag = tagList.getCompound(index);
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
                updateShulkerBoxContainer(player, shulkerBox, tagList, index, stack);
                return picked;
            }
        }
        return 0;
    }

    public static boolean replenishmentTool(Player player, EquipmentSlot slot, List<ItemStack> shulkerItems, Predicate<ItemStack> predicate) {
        for (ItemStack shulkerBox : shulkerItems) {
            CompoundTag nbt = shulkerBox.getTagElement("BlockEntityTag");
            if (nbt == null) continue;
            ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
            for (int index = 0; index < tagList.size(); index++) {
                CompoundTag tag = tagList.getCompound(index);
                ItemStack stack = ItemStack.of(tag);
                if (!predicate.test(stack)) continue;
                ItemStack itemBySlot = player.getItemBySlot(slot);
                ItemStack copy = stack.copy();
                player.setItemSlot(slot, copy);
                updateShulkerBoxContainer(player, shulkerBox, tagList, index, itemBySlot);
                return true;
            }
        }
        return false;
    }

    private static void updateShulkerBoxContainer(Player player, ItemStack shulkerBox, ListTag listTag, int index, ItemStack stack) {
        if (shulkerBox.getCount() > 1) {
            ItemStack newShulker = shulkerBox.copyWithCount(1);
            shulkerBox.shrink(1);
            if (!player.addItem(newShulker)) {
                ItemEntity itemEntity = player.drop(newShulker, false);
                if (itemEntity != null) {
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setTarget(player.getUUID());
                }
            }
            shulkerBox = newShulker;
        }

        CompoundTag nbt = shulkerBox.getTagElement("BlockEntityTag");
        assert nbt != null;

        if (stack.isEmpty()) {
            listTag.remove(index);
            if (listTag.isEmpty()) nbt.remove("Items");
        } else {
            CompoundTag newTag = stack.save(new CompoundTag());
            newTag.putByte("Slot", listTag.getCompound(index).getByte("Slot"));
            listTag.set(index, newTag);
            nbt.put("Items", listTag);
        }
    }


}
