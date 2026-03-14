package dev.dubhe.gugle.carpet.config.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.entry.BotActionInfo;
import dev.dubhe.gugle.carpet.entry.BotGroupInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.entry.IWithName;
import dev.dubhe.gugle.carpet.entry.LocationInfo;
import dev.dubhe.gugle.carpet.entry.NameBooleanInfo;
import dev.dubhe.gugle.carpet.entry.TodoInfo;
import dev.dubhe.gugle.carpet.tools.ResourceLocationSerializer;
import dev.dubhe.gugle.carpet.tools.serializer.ChatFormattingSerializer;
import dev.dubhe.gugle.carpet.tools.serializer.DimTypeSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigUpdater {
    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeHierarchyAdapter(ResourceKey.class, new DimTypeSerializer())
        .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocationSerializer())
        .registerTypeHierarchyAdapter(ChatFormatting.class, new ChatFormattingSerializer())
        .create();

    public static void tryUpdateOldVersion(LevelStorageSource.LevelStorageAccess access) {
        Path levelPath = access.getLevelPath(LevelResource.ROOT);
        // bot
        updateMode1(NameMapper.of(levelPath, "bot"), DeprecatedBotInfo.class, BotInfo.CODEC, info -> {
            BotActionInfo actions = BotActionInfo.CODEC.parse(JsonOps.INSTANCE, info.actions).result().orElse(BotActionInfo.EMPTY);
            return new BotInfo(
                info.name,
                info.desc,
                info.pos,
                info.facing,
                info.dimension,
                info.mode,
                info.flying,
                actions
            );
        });

        updateMode1(NameMapper.of(levelPath, "botGroup", "bot_group"), BotGroupInfo.class, BotGroupInfo.CODEC, info -> info);
        updateMode1(NameMapper.of(levelPath, "todo"), TodoInfo.class, TodoInfo.CODEC, info -> info);

        updateMode1(NameMapper.of(levelPath, "loc", "location"), DeprecatedLocInfo.class, LocationInfo.CODEC, info ->
            new LocationInfo(info.id, info.desc, new Vec3(info.x, info.y, info.z), info.dimension));

        updateMode2(NameMapper.of(levelPath, "blist"));
        updateMode2(NameMapper.of(levelPath, "wlist"));



    }

    private static <T, R extends IWithName> void updateMode1(NameMapper fileMapper, Class<T> tClass, Codec<R> codec, Function<T, R> function) {
        updateResolver(fileMapper, codec, entry -> function.apply(GSON.fromJson(entry.getValue(), tClass)));
    }

    private static void updateMode2(NameMapper fileMapper) {
        updateResolver(fileMapper, NameBooleanInfo.CODEC, it -> new NameBooleanInfo(it.getKey(), it.getValue().getAsBoolean()));
    }

    private static <T extends IWithName> void updateResolver(NameMapper fileMapper, Codec<T> codec, Function<Map.Entry<String, JsonElement>, T> function) {
        if (!fileMapper.oldPath.toFile().exists()) return;
        try {
            String json = Files.readString(fileMapper.oldPath);
            Map<String, T> contents = GSON.fromJson(json, JsonObject.class)
                .entrySet()
                .stream()
                .map(function)
                .collect(Collectors.toMap(IWithName::name, it -> it));

            Codec<Map<String, T>> resultCodec = Codec.unboundedMap(Codec.STRING, codec);
            DataResult<JsonElement> result = resultCodec.encodeStart(JsonOps.INSTANCE, contents);

            if (result.error().isPresent()) {
                GcaExtension.LOGGER.error("Failed to encode config: {}", result.error().get().message());
                return;
            }

            JsonElement resultJson = result.result().orElseThrow();
            Files.writeString(fileMapper.newName, GcaExtension.GSON.toJson(resultJson), StandardCharsets.UTF_8);
        } catch (Exception e) {
            GcaExtension.LOGGER.warn("Failed to update old config: {}", fileMapper.oldPath, e);
        }
    }

    private record NameMapper(Path oldPath, Path newName) {

        public static NameMapper of(Path root, String name) {
            return create(root, name + ".gca.json", name + ".json");
        }

        public static NameMapper of(Path root, String oldName, String newName) {
            return create(root, oldName + ".gca.json", newName + ".json");
        }

        private static NameMapper create(Path root, String oldName, String newName) {
            return new NameMapper(
                root.resolve(oldName),
                root.resolve("serverconfig").resolve(GcaExtension.MOD_NAME.toLowerCase()).resolve(newName)
            );
        }
    }

    private record DeprecatedBotInfo(
        String name,
        String desc,
        Vec3 pos,
        Vec2 facing,
        @SerializedName("dim_type") ResourceKey<Level> dimension,
        GameType mode,
        boolean flying,
        JsonObject actions
    ) { }

    public record DeprecatedLocInfo(
        long id,
        String desc,
        double x,
        double y,
        double z,
        @SerializedName("dim_type") ResourceKey<Level> dimension
    ) { }
}
