package dev.dubhe.gugle.carpet.tools;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import dev.dubhe.gugle.carpet.GcaExtension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public abstract class FilesUtil {
    private static final Gson GSON = GcaExtension.GSON;
    public MinecraftServer server = null;
    private final String gcaJson;

    public FilesUtil(String jsonPrefix) {
        this.gcaJson = "%s.gca.json".formatted(jsonPrefix);
    }

    public void init(CommandContext<CommandSourceStack> context) {
        MinecraftServer server1 = context.getSource().getServer();
        this.init(server1);
    }

    protected abstract void createDefault(File file) throws IOException;

    protected abstract void init(BufferedReader bfr);

    public void init(MinecraftServer server1) {
        if (server1 == server) return;
        this.server = server1;
        File file = this.server.getWorldPath(LevelResource.ROOT).resolve(this.gcaJson).toFile();
        try {
            if (!file.exists()) {
                this.createDefault(file);
                return;
            }
            try (BufferedReader bfr = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                this.init(bfr);
            }
        } catch (IOException e) {
            GcaExtension.LOGGER.error(e.getMessage(), e);
        }
    }

    protected abstract void save(BufferedWriter bw);

    public void save() {
        if (this.server == null) return;
        File file = this.server.getWorldPath(LevelResource.ROOT).resolve(this.gcaJson).toFile();
        try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            this.save(bw);
        } catch (IOException e) {
            GcaExtension.LOGGER.error(e.getMessage(), e);
        }
    }

    public static class MapFile<K extends Comparable<K>, V> extends FilesUtil {
        public final Map<K, V> map = new TreeMap<>();
        private final Function<String, K> keyCodec;
        private final Class<V> vClass;

        public MapFile(String jsonPrefix, Function<String, K> keyCodec, Class<V> vClass) {
            super(jsonPrefix);
            this.keyCodec = keyCodec;
            this.vClass = vClass;
        }

        @Override
        protected void createDefault(File file) throws IOException {
            try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                this.save(bw);
            }
        }

        @Override
        protected void init(BufferedReader bfr) {
            this.map.clear();
            for (Map.Entry<String, JsonElement> entry : FilesUtil.GSON.fromJson(bfr, JsonObject.class).entrySet()) {
                this.map.put(keyCodec.apply(entry.getKey()), FilesUtil.GSON.fromJson(entry.getValue(), this.vClass));
            }
        }

        @Override
        protected void save(BufferedWriter bw) {
            FilesUtil.GSON.toJson(this.map, bw);
        }
    }

    public static class ObjFile<T> extends FilesUtil {
        public T obj;

        public ObjFile(String jsonPrefix, T defaultObj) {
            super(jsonPrefix);
            this.obj = defaultObj;
        }

        @Override
        protected void createDefault(File file) throws IOException {
            try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                this.save(bw);
            }
        }

        @Override
        protected void init(BufferedReader bfr) {
            //noinspection unchecked
            this.obj = (T) FilesUtil.GSON.fromJson(bfr, this.obj.getClass());
        }

        @Override
        protected void save(BufferedWriter bw) {
            FilesUtil.GSON.toJson(this.obj, bw);
        }
    }
}
