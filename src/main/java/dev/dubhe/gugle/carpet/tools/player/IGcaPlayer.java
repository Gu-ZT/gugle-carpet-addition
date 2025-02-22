package dev.dubhe.gugle.carpet.tools.player;

public interface IGcaPlayer {
    default PlayerEnderChestContainer getEnderChestContainer() {
        throw new AssertionError();
    }

    default  PlayerInventoryContainer getInventoryContainer() {
        throw new AssertionError();
    }
}
