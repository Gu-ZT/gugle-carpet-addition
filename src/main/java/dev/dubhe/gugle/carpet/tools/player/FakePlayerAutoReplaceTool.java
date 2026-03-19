package dev.dubhe.gugle.carpet.tools.player;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

//#if MC >= 12100
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
//#else
//$$ import net.minecraft.world.item.enchantment.EnchantmentHelper;
//$$ import net.minecraft.world.item.enchantment.Enchantments;
//#endif

//#if MC < 12005
//$$ import net.minecraft.nbt.CompoundTag;
//$$ import net.minecraft.nbt.ListTag;
//$$ import net.minecraft.nbt.Tag;
//$$ import java.util.Optional;
//#else
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import java.util.List;
import java.util.stream.Collectors;
//#endif

public class FakePlayerAutoReplaceTool {
    @SuppressWarnings("UnnecessaryReturnStatement")
    //#if MC>=12005
    public static void autoReplaceTool(Player fakePlayer, Item item, EquipmentSlot equipmentSlot) {
        ItemStack itemStack = fakePlayer.getItemBySlot(equipmentSlot);
    //#else
    //$$ public static void autoReplaceTool(Player fakePlayer, Item item, ItemStack itemStack) {
    //#endif
        Predicate<ItemStack> isDamageItem = itemDamagePredicate();
        if (isDamageItem.test(itemStack)) {
            //#if MC < 12005
            //$$ Optional<EquipmentSlot> optional = getEquipmentSlot(fakePlayer, itemStack);
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

    //#if MC<12005
    //$$ private static Optional<EquipmentSlot> getEquipmentSlot(Player fakePlayer, ItemStack itemStack) {
    //$$     for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
    //$$         if (fakePlayer.getItemBySlot(equipmentSlot) == itemStack) {
    //$$             return Optional.of(equipmentSlot);
    //$$         }
    //$$     }
    //$$     return Optional.empty();
    //$$ }
    //#endif

    public static boolean replaceTool(EquipmentSlot slot, Predicate<ItemStack> predicate, Player fakePlayer) {
        if (predicate.test(fakePlayer.getItemBySlot(slot))) {
            return true;
        }
        Inventory inventory = fakePlayer.getInventory();
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
            }
        }
        // 从潜影盒补货
        if (!GcaSetting.fakePlayerAutoReplenishmentFormShulkerBox) return false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.is(Items.SHULKER_BOX)) continue;
            //#if MC>=12005
            ItemContainerContents contents = itemStack.get(DataComponents.CONTAINER);
            if (contents == null) continue;
            List<ItemStack> items = contents.stream().collect(Collectors.toList());
            for (int index = 0; index < items.size(); index++) {
                ItemStack item = items.get(index);
                if (predicate.test(item)) {
                    ItemStack itemBySlot = fakePlayer.getItemBySlot(slot);
                    ItemStack copy = item.copy();
                    items.set(index, itemBySlot);
                    fakePlayer.setItemSlot(slot, copy);
                    itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
                    return true;
                }
            }
            //#else
            //$$ CompoundTag nbt = itemStack.getTagElement("BlockEntityTag");
            //$$ if (nbt == null || !nbt.contains("Items", Tag.TAG_LIST)) return false;
            //$$ ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
            //$$ for (int index = 0; index < tagList.size(); index++) {
            //$$     CompoundTag tag = tagList.getCompound(index);
            //$$     ItemStack stack = ItemStack.of(tag);
            //$$     if (predicate.test(stack)) {
            //$$         ItemStack itemBySlot = fakePlayer.getItemBySlot(slot);
            //$$         ItemStack copy = stack.copy();
            //$$         fakePlayer.setItemSlot(slot, copy);
            //$$         CompoundTag newTag = itemBySlot.save(new CompoundTag());
            //$$         newTag.putByte("Slot", tag.getByte("Slot"));
            //$$         tagList.set(index, newTag);
            //$$     }
            //$$ }
            //#endif
        }
        return false;
    }

    private static Predicate<ItemStack> itemDamagePredicate() {
        boolean keepTool = "keep".equals(GcaSetting.fakePlayerAutoReplaceTool);
        return itemStack -> {
            if (itemStack.isEmpty()) {
                return true;
            }
            if (itemStack.isDamageableItem()) {
                // 有经验修补的物品，保留10点耐久
                if (keepTool || hasMending(itemStack)) {
                    int remaining = itemStack.getMaxDamage() - itemStack.getDamageValue();
                    return remaining <= 10;
                }
                return false;
            }
            return false;
        };
    }

    private static Predicate<ItemStack> itemReplacePredicate(Item item) {
        boolean keepTool = "keep".equals(GcaSetting.fakePlayerAutoReplaceTool);
        return itemStack -> {
            if (itemStack.getItem().getClass() == item.getClass()) {
                if (keepTool || hasMending(itemStack)) {
                    return itemStack.getMaxDamage() - itemStack.getDamageValue() > 10;
                }
                return true;
            }
            return false;
        };
    }

    private static boolean hasMending(ItemStack itemStack) {
        //#if MC>=12100
        ItemEnchantments enchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Holder<Enchantment> key = entry.getKey();
            if (key.value().effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP)) {
                return true;
            }
        }
        return false;
        //#else
        //$$ return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, itemStack) > 0;
        //#endif
    }
}
