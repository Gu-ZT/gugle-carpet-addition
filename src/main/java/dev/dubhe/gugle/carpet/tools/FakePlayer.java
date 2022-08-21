package dev.dubhe.gugle.carpet.tools;

import carpet.CarpetSettings;
import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaExtension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class FakePlayer {

    public static class ComponentTrans {

        private static final Map<String, String> lang = new GcaExtension().canHasTranslations(
                CarpetSettings.language);

        public static Component trans(String key, Object... args) {
            return Component.literal(ComponentTrans.lang.get(key).formatted(args))
                    .setStyle(Style.EMPTY.withBold(true).withItalic(false));
        }
    }

    public static boolean checkButton(int slot, boolean flag, Container container, int itemCount, Component on,
            Component off, @Nullable BiConsumer<Container, Integer> onFunc,
            @Nullable BiConsumer<Container, Integer> offFunc, ServerPlayer player, boolean... a) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("GcaClear", true);
        ItemStack button_off = new ItemStack(Items.STRUCTURE_VOID, itemCount);
        ItemStack button_on = new ItemStack(Items.BARRIER, itemCount);
        button_off.setTag(compoundTag);
        button_on.setTag(compoundTag);
        ItemStack item = container.getItem(slot);
        if (item.getItem() != Items.STRUCTURE_VOID && item.getItem() != Items.BARRIER) {
            player.drop(item, false, false);
            item = container.getItem(slot);
        }
        if (item.isEmpty() && flag) {
            if (null != offFunc) {
                offFunc.accept(container, slot);
            }
            container.setItem(slot, button_off);
        } else if (item.isEmpty()) {
            if (null != onFunc) {
                onFunc.accept(container, slot);
            }
            if (a.length > 0 && !a[0]) {
                container.setItem(slot, button_off);
            } else {
                container.setItem(slot, button_on);
            }
        } else if (flag) {
            container.setItem(slot, button_on);
        } else {
            container.setItem(slot, button_off);
        }
        item = container.getItem(slot);
        boolean bl = item.is(Items.BARRIER);
        if (!bl) {
            item.setHoverName(off.copy());
        } else {
            item.setHoverName(on);
        }
        return bl;
    }

    public static void syncItem(EntityPlayerMPFake fakePlayer, SimpleContainer container) {
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = fakePlayer.getInventory().getItem(i);
            if (itemStack == container.getItem(i + 9) || container.getItem(i + 9).isEmpty()) {
                container.setItem(i + 9, itemStack);
            } else {
                fakePlayer.getInventory().setItem(i, container.getItem(i + 9));
            }
        }
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = fakePlayer.getInventory().getItem(i);
            if (itemStack == container.getItem(i + 45) || container.getItem(i + 45).isEmpty()) {
                container.setItem(i + 45, itemStack);
            } else {
                fakePlayer.getInventory().setItem(i, container.getItem(i + 45));
            }
        }
        for (int i = 0; i < 4; i++) {
            ItemStack itemStack = fakePlayer.getInventory().getArmor(i);
            if (itemStack == container.getItem(4 - i) || container.getItem(4 - i).isEmpty()) {
                container.setItem(4 - i, itemStack);
            } else {
                fakePlayer.getInventory().armor.set(i, container.getItem(4 - i));
            }
        }
        ItemStack itemStack = fakePlayer.getOffhandItem();
        if (itemStack == container.getItem(7) || container.getItem(7).isEmpty()) {
            container.setItem(7, itemStack);
        } else {
            fakePlayer.setItemSlot(EquipmentSlot.OFFHAND, container.getItem(7));
        }
    }

}
