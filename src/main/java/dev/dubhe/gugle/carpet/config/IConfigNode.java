package dev.dubhe.gugle.carpet.config;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public interface IConfigNode {
    String name();

    Component component(MinecraftServer server);
}
