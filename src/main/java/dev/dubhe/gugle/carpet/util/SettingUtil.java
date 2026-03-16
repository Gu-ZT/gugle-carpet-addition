package dev.dubhe.gugle.carpet.util;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class SettingUtil {
    public static boolean openFakePlayerEnderChest(Player player) {
        if ("true".equals(GcaSetting.openFakePlayerEnderChest)) return true;
        return "ender_chest".equals(GcaSetting.openFakePlayerEnderChest) &&
            (player.getMainHandItem().is(Items.ENDER_CHEST) || player.getOffhandItem().is(Items.ENDER_CHEST));
    }
}
