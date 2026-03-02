package dev.dubhe.gugle.carpet.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.entry.IWithName;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class GcaConfig<T extends IWithName> {
    public static final Map<String, GcaConfig<?>> CONFIGS = new LinkedHashMap<>();

    private final Map<String, T> contents = new LinkedHashMap<>();
    private MinecraftServer server;
    private final String filename;
    private final Codec<Map<String, T>> codec;

    private GcaConfig(String name, Codec<T> codec) {
        this.filename = name + ".json";
        this.codec = Codec.unboundedMap(Codec.STRING, codec);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IWithName> GcaConfig<T> create(String name, Codec<T> codec) {
        return (GcaConfig<T>) CONFIGS.computeIfAbsent(name, key -> new GcaConfig<>(name, codec));
    }

    public void update(T value) {
        this.update(value, true);
    }

    public void update(T value, boolean dirty) {
        this.contents.put(value.name(), value);
        if (dirty) this.setDirty();
    }

    public T remove(String name) {
        if (!this.contents.containsKey(name)) return null;
        T removed = this.contents.remove(name);
        this.setDirty();
        return removed;
    }

    public Map<String, T> getContents() {
        return this.contents;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public void tryInit(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        this.tryInit(server);
    }

    public void tryInit(MinecraftServer server) {
        if (server == this.server) return;
        this.server = server;
        this.contents.clear();

        Path path = this.server.getWorldPath(LevelResource.ROOT)
            .resolve("serverconfig")
            .resolve(GcaExtension.MOD_ID)
            .resolve(this.filename);

        try {
            String json = this.getOrCreateFile(path);
            this.codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .result()
                .ifPresent(this.contents::putAll);
        } catch (IOException e) {
            GcaExtension.LOGGER.error("Failed to create config file: {}", this.filename, e);
        }
    }

    public String getOrCreateFile(Path path) throws IOException {
        File file = path.toFile();
        if (!file.exists() || !file.isFile()) {
            file.getParentFile().mkdirs();
            Files.writeString(path, "{}", StandardCharsets.UTF_8);
            return "{}";
        }
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    public void setDirty() {
        if (this.server == null) return;

        Path path = this.server.getWorldPath(LevelResource.ROOT)
            .resolve("serverconfig")
            .resolve(GcaExtension.MOD_ID)
            .resolve(this.filename);

        try {
            path.toFile().getParentFile().mkdirs();
            DataResult<JsonElement> result = this.codec.encodeStart(JsonOps.INSTANCE, this.contents);
            if (result.error().isPresent()) {
                GcaExtension.LOGGER.error("Failed to encode config: {}", result.error().get().message());
                return;
            }

            JsonElement json = result.result().orElseThrow();
            Files.writeString(path, GcaExtension.GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (IOException e) {
            GcaExtension.LOGGER.error("Failed to save config file: {}", this.filename, e);
        }
    }
}
