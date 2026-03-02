package dev.dubhe.gugle.carpet.config;

import com.mojang.brigadier.context.CommandContext;
import dev.dubhe.gugle.carpet.GcaExtension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class GcaConfig {
    private MinecraftServer server;
    private final String filename;

    public GcaConfig(String name) {
        this.filename = name + ".gca.json";
    }

    public void init(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        this.init(server);
    }

    public void init(MinecraftServer server) {
        if (server == this.server) return;
        this.server = server;
        File file = this.server.getWorldPath(LevelResource.ROOT).resolve(this.filename).toFile();
//        try {
//            if (!file.exists()) {
//                this.createDefault(file);
//                return;
//            }
//            try (BufferedReader bfr = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
//                this.init(bfr);
//            }
//        } catch (IOException e) {
//            GcaExtension.LOGGER.error(e.getMessage(), e);
//        }

    }
}
