package dev.dubhe.gugle.carpet;

import carpet.settings.Rule;

public class GcaSetting {

    public static final String GCA = "GCA";
    public static final String EXPERIMENTAL = "experimental";

    // 允许玩家打开假人背包
    @Rule(
            desc = "Allow player to open the fake player's inventory",
            category = {GCA, EXPERIMENTAL}
    )
    public static boolean openFakePlayerInventory = false;

    // 允许玩家打开假人末影箱
    @Rule(
            desc = "Allow player to open the fake player's ender chest",
            category = {GCA, EXPERIMENTAL}
    )
    public static boolean openFakePlayerEnderChest = false;
}
