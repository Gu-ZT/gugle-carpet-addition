package dev.dubhe.gugle.carpet.tools.player;

import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.util.ContainerUtil;
import dev.dubhe.gugle.carpet.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
//#if MC < 12005
//$$ import java.util.Optional;
//#endif

public class FakePlayerAutoReplaceTool {
    private static final Map<UUID, ReplaceInfo> FAKE_PLAYER_TOOL_MAP = new HashMap<>();

    public static void clear() {
        FAKE_PLAYER_TOOL_MAP.clear();
    }

    //#if MC>=12005
    public static void checkFakePlayerShouldReplaceTool(ServerPlayer player, Item item, EquipmentSlot slot) {
        ItemStack itemStack = player.getItemBySlot(slot);
        //#else
        //$$ public static void checkFakePlayerShouldReplaceTool(Player player, Item item, ItemStack itemStack) {
        //#endif
        if (itemStack.isEmpty() || (
            itemStack.isDamageableItem() &&
                ("keep".equals(GcaSetting.fakePlayerAutoReplaceTool) || InventoryUtil.hasMendingEnchant(itemStack)) &&
                itemStack.getMaxDamage() - itemStack.getDamageValue() <= 10
        )) {
            //#if MC < 12005
            //$$ Optional<EquipmentSlot> optional = InventoryUtil.getEquipmentSlot(player, itemStack);
            //$$ if (optional.isEmpty()) {
            //$$     return;
            //$$ }
            //$$ EquipmentSlot slot = optional.get();
            //#endif
            FAKE_PLAYER_TOOL_MAP.put(player.getUUID(), new ReplaceInfo(item, slot));
        }
    }

    public static void tryReplaceTool(ServerPlayer player) {
        UUID uuid = player.getUUID();
        ReplaceInfo info = FAKE_PLAYER_TOOL_MAP.remove(uuid);
        if (info == null) return;
        Predicate<ItemStack> predicate = itemReplacePredicate(info.item);
        if (!replaceTool(info.slot, predicate, player) && GcaSetting.fakePlayerToolDamagedNotification) {
            FakePlayerNotification.sendRestockFailed(player, info.item);
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
                inventory.setItem(i, itemBySlot.isEmpty() ? ItemStack.EMPTY : itemBySlot);
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
        return itemStack -> itemStack.is(item) &&
            ((!keepTool && !InventoryUtil.hasMendingEnchant(itemStack)) ||
                itemStack.getMaxDamage() - itemStack.getDamageValue() > 10);
    }

    private record ReplaceInfo(Item item, EquipmentSlot slot) {
    }
}
