package dev.dubhe.gugle.carpet.api.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;

public class CustomMenuType {

    public static final MenuType<ChestMenu> GENERIC_9x1 = MenuType.GENERIC_9x1;
    public static final MenuType<ChestMenu> GENERIC_9x2 = MenuType.GENERIC_9x2;
    public static final MenuType<ChestMenu> GENERIC_9x3 = MenuType.GENERIC_9x3;
    public static final MenuType<ChestMenu> GENERIC_9x4 = MenuType.GENERIC_9x4;
    public static final MenuType<ChestMenu> GENERIC_9x5 = MenuType.GENERIC_9x5;
    public static final MenuType<ChestMenu> GENERIC_9x6 = MenuType.GENERIC_9x6;
    public static final MenuType<DispenserMenu> GENERIC_3x3 = MenuType.GENERIC_3x3;
    public static final MenuType<HopperMenu> HOPPER = MenuType.HOPPER;

    public static int getCount(Type type) {
        switch (type) {
            case GENERIC_9x2 -> {
                return 18;
            }
            case GENERIC_9x3 -> {
                return 27;
            }
            case GENERIC_9x4 -> {
                return 36;
            }
            case GENERIC_9x5 -> {
                return 45;
            }
            case GENERIC_9x6 -> {
                return 54;
            }
            case HOPPER -> {
                return 5;
            }
            default -> {
                return 9;
            }
        }
    }

    public static AbstractContainerMenu getMenu(Type type, int id, Inventory inventory, Container container) {
        switch (type) {
            case GENERIC_9x2 -> {
                return new ChestMenu(MenuType.GENERIC_9x2, id, inventory, container, 2);
            }
            case GENERIC_9x3 -> {
                return new ChestMenu(MenuType.GENERIC_9x3, id, inventory, container, 3);
            }
            case GENERIC_9x4 -> {
                return new ChestMenu(MenuType.GENERIC_9x4, id, inventory, container, 4);
            }
            case GENERIC_9x5 -> {
                return new ChestMenu(MenuType.GENERIC_9x5, id, inventory, container, 5);
            }
            case GENERIC_9x6 -> {
                return new ChestMenu(MenuType.GENERIC_9x6, id, inventory, container, 6);
            }
            case HOPPER -> {
                return new HopperMenu(id, inventory, container);
            }
            default -> {
                return new ChestMenu(MenuType.GENERIC_9x1, id, inventory, container, 1);
            }
        }
    }

    public enum Type {
        GENERIC_9x1,
        GENERIC_9x2,
        GENERIC_9x3,
        GENERIC_9x4,
        GENERIC_9x5,
        GENERIC_9x6,
        GENERIC_3x3,
        HOPPER
    }
}
