package dev.dubhe.gugle.carpet.tools;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FakePlayerAutoReplenishment {

    public static void autoReplenishment(Player fakePlayer) {
        NonNullList<ItemStack> itemStackList = fakePlayer.getInventory().items;
        replenishment(fakePlayer.getMainHandItem(), itemStackList);
        replenishment(fakePlayer.getOffhandItem(), itemStackList);
    }

    public static void replenishment(ItemStack itemStack, NonNullList<ItemStack> itemStackList) {
        int count = itemStack.getMaxStackSize() / 2;
        if (!itemStack.isEmpty() && itemStack.getCount() <= 8 && count > 8) {
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
                    if (nbt != null) {
                        if (nbt.contains("Items", Tag.TAG_LIST)) {
                            ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
                            for (int i = 0; i < tagList.size(); ++i) {
                                CompoundTag tag = tagList.getCompound(i);
                                ItemStack stack = ItemStack.of(tag);
                                if (ItemStack.isSameItemSameTags(stack, itemStack)) {
                                    if (stack.getCount() > count) {
                                        itemStack.setCount(itemStack.getCount() + count);
                                        stack.setCount(stack.getCount() - count);
                                    } else {
                                        itemStack.setCount(itemStack.getCount() + stack.getCount());
                                        stack.setCount(0);
                                    }
                                    if (stack.isEmpty()) {
                                        tagList.remove(i);
                                    } else {
                                        CompoundTag newTag = stack.save(new CompoundTag());
                                        newTag.putByte("Slot", tag.getByte("Slot"));
                                        tagList.set(i, newTag);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
