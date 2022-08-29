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

    // 退出存档时保留假人
    @Rule(
            desc = "Keep the fake player when exiting the level",
            category = {GCA, EXPERIMENTAL}
    )
    public static boolean fakePlayerResident = false;

    // 让假人自动补货
    @Rule(
            desc = "Make fake player to auto replenishment",
            category = {GCA, EXPERIMENTAL}
    )
    public static boolean fakePlayerAutoReplenishment = false;

    // 让假人自动钓鱼
    @Rule(
            desc = "Make fake player to auto fish",
            category = {GCA, EXPERIMENTAL}
    )
    public static boolean fakePlayerAutoFish = false;
}
