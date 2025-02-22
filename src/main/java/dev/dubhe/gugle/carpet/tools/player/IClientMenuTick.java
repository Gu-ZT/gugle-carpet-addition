package dev.dubhe.gugle.carpet.tools.player;

public interface IClientMenuTick {
    default void tick() {
        throw new AssertionError();
    }
}
