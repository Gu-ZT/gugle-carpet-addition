package dev.dubhe.gugle.carpet;

import carpet.api.settings.Rule;

public class GcaSetting {

    public static final String GCA = "GCA";
    public static final String EXPERIMENTAL = "experimental";

    // 允许玩家打开假人背包
    @Rule(
            categories = {GCA, EXPERIMENTAL}
    )
    public static boolean openFakePlayerInventory = false;

    // 允许玩家打开假人末影箱
    @Rule(
            categories = {GCA, EXPERIMENTAL}
    )
    public static boolean openFakePlayerEnderChest = false;

    // 退出存档时保留假人
    @Rule(
            categories = {GCA, EXPERIMENTAL}
    )
    public static boolean fakePlayerResident = false;

    // 让假人自动补货
    @Rule(
            categories = {GCA, EXPERIMENTAL}
    )
    public static boolean fakePlayerAutoReplenishment = false;

    // 让假人自动钓鱼
    @Rule(
            categories = {GCA, EXPERIMENTAL}
    )
    public static boolean fakePlayerAutoFish = false;

    // 让假人自动切换快损坏的工具
    @Rule(
            categories = {GCA, EXPERIMENTAL}
    )
    public static boolean fakePlayerAutoReplaceTool = false;
}
