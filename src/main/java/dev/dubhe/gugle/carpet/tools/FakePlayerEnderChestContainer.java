package dev.dubhe.gugle.carpet.tools;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class FakePlayerEnderChestContainer extends ChestMenu {
    private final EntityPlayerMPFake fakePlayer;

    public FakePlayerEnderChestContainer(int i, Inventory inventory, EntityPlayerMPFake fakePlayer) {
        super(MenuType.GENERIC_9x3, i, inventory, fakePlayer.getEnderChestInventory(), 3);
        this.fakePlayer = fakePlayer;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (fakePlayer.isRemoved()) {
            return false;
        }
        return !(player.distanceToSqr(this.fakePlayer) > 64.0);
    }
}
