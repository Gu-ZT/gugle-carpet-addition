package dev.dubhe.gugle.carpet.tools.player;

import net.minecraft.resources.ResourceLocation;

public interface ISlotIcon {
    default void setIcon(ResourceLocation resource) {
        throw new AssertionError();
    }
}
