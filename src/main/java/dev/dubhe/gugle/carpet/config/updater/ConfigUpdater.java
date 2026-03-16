package dev.dubhe.gugle.carpet.config.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.BotActionInfo;
import dev.dubhe.gugle.carpet.entry.BotGroupInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.entry.IWithName;
import dev.dubhe.gugle.carpet.entry.LocationInfo;
import dev.dubhe.gugle.carpet.entry.NameBooleanInfo;
import dev.dubhe.gugle.carpet.entry.TodoInfo;
import dev.dubhe.gugle.carpet.entry.WelcomeInfo;
import dev.dubhe.gugle.carpet.config.updater.serializer.ResourceLocationSerializer;
import dev.dubhe.gugle.carpet.config.updater.serializer.ChatFormattingSerializer;
import dev.dubhe.gugle.carpet.config.updater.serializer.DimTypeSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

//#if MC >= 12003
import net.minecraft.nbt.NbtAccounter;
//#endif
//#if MC < 12105
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
//#endif
//#if MC < 12109
import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.GameProfileCache;

//#else
//$$ import net.minecraft.server.players.NameAndId;
//$$ import net.minecraft.util.StringUtil;
//#endif
public class ConfigUpdater {
    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeHierarchyAdapter(ResourceKey.class, new DimTypeSerializer())
        .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocationSerializer())
        .registerTypeHierarchyAdapter(ChatFormatting.class, new ChatFormattingSerializer())
        .create();

    public static void tryUpdateOldVersion(LevelStorageSource.LevelStorageAccess access, Services services, boolean onelineMode) {
        Path levelPath = access.getLevelPath(LevelResource.ROOT);
        GcaExtension.LOGGER.info("Checking old config files...");

        updateMapping(NameMapper.of(levelPath, "bot"), DeprecatedBotInfo.class, BotInfo.CODEC, info -> {
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

        updateMapping(NameMapper.of(levelPath, "botGroup", "bot_group"), BotGroupInfo.class, BotGroupInfo.CODEC, info -> info);
        updateMapping(NameMapper.of(levelPath, "todo"), TodoInfo.class, TodoInfo.CODEC, info -> info);

        updateMapping(NameMapper.of(levelPath, "loc", "location"), DeprecatedLocInfo.class, LocationInfo.CODEC, info ->
            new LocationInfo(info.id, info.desc, new Vec3(info.x, info.y, info.z), info.dimension));

        updateNameBoolean(NameMapper.of(levelPath, "blist"));
        updateNameBoolean(NameMapper.of(levelPath, "wlist"));

        updateWelcome(NameMapper.of(levelPath, "welcome"));

        updateResident(NameMapper.of(levelPath, "fake_player", "residents"), access, services, onelineMode);
    }

    private static <T, R extends IWithName> void updateMapping(NameMapper fileMapper, Class<T> tClass, Codec<R> codec, Function<T, R> function) {
        updateNormalResolver(fileMapper, codec, entry -> function.apply(GSON.fromJson(entry.getValue(), tClass)));
    }

    private static void updateNameBoolean(NameMapper fileMapper) {
        updateNormalResolver(fileMapper, NameBooleanInfo.CODEC, it -> new NameBooleanInfo(it.getKey(), it.getValue().getAsBoolean()));
    }

    private static void updateWelcome(NameMapper fileMapper) {
        updateResolver(fileMapper, WelcomeInfo.CODEC, json -> {
            List<String> messages = json.get("message").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();
            Map<String, WelcomeInfo.MessageArg> args = json.get("args").getAsJsonObject()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, it -> {
                    JsonElement element = it.getValue();
                    if (element.isJsonPrimitive()) {
                        return new WelcomeInfo.MessageArg(GcaExtension.parseLocation(element.getAsString()));
                    }
                    JsonObject object = json.getAsJsonObject();
                    ChatFormatting color = Optional.ofNullable(object.get("color"))
                        .map(key -> ChatFormatting.getByName(key.getAsString()))
                        .orElse(ChatFormatting.GOLD);
                    return new WelcomeInfo.MessageArg(
                        GcaExtension.parseLocation(object.get("type").getAsString()),
                        Optional.ofNullable(object.get("data")),
                        color
                    );
                }));

            return List.of(new WelcomeInfo(messages, args));
        });
    }

    private static void updateResident(NameMapper fileMapper, LevelStorageSource.LevelStorageAccess access, Services services, boolean onelineMode) {
        File playerDir = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).toFile();
        updateNormalResolver(fileMapper, BotInfo.CODEC, entry -> {
            String name = entry.getKey();
            JsonObject value = entry.getValue().getAsJsonObject();
            UUID uuid = getUUIDByName(services, name, onelineMode);
            if (uuid == null) {
                GcaExtension.LOGGER.warn("Failed to get UUID for {}, skipping...", name);
                return null;
            }
            File file = new File(playerDir, uuid + ".dat");
            if (!file.exists() || !file.isFile()) {
                GcaExtension.LOGGER.warn("Player data file not found for {}, skipping...", name);
                return null;
            }
            Optional<CompoundTag> optional = getPlayerData(file);
            if (optional.isEmpty()) {
                GcaExtension.LOGGER.warn("Failed to load player data for {}, skipping...", name);
                return null;
            }
            BotActionInfo actions = BotActionInfo.CODEC.parse(JsonOps.INSTANCE, value.get("actions")).result().orElse(BotActionInfo.EMPTY);
            return parseResidentBotInfo(name, actions, optional.get());
        });
    }

    private static <T extends IWithName> void updateNormalResolver(NameMapper fileMapper, Codec<T> codec, Function<Map.Entry<String, JsonElement>, T> function) {
        updateResolver(fileMapper, codec, json -> GSON.fromJson(json, JsonObject.class)
            .entrySet()
            .stream()
            .map(function)
            .filter(Objects::nonNull)
            .toList());
    }

    private static <T extends IWithName> void updateResolver(NameMapper fileMapper, Codec<T> codec, Function<JsonObject, List<T>> function) {
        if (!fileMapper.oldPath.toFile().exists()) return;
        GcaExtension.LOGGER.info("Found old config file: {}, trying to update...", fileMapper.oldPath);
        try {
            String jsonStr = Files.readString(fileMapper.oldPath);
            JsonObject json = GSON.fromJson(jsonStr, JsonObject.class);

            List<T> list = function.apply(json);

            if (!list.isEmpty()) {
                Map<String, T> contents = new LinkedHashMap<>();

                Codec<Map<String, T>> fileCodec = Codec.unboundedMap(Codec.STRING, codec);
                if (fileMapper.newName.toFile().exists()) {
                    String already = Files.readString(fileMapper.newName, StandardCharsets.UTF_8);
                    fileCodec.parse(JsonOps.INSTANCE, JsonParser.parseString(already))
                        .result()
                        .ifPresent(contents::putAll);
                }

                for (T data : list) {
                    String name = data.name();
                    if (contents.containsKey(name)) continue;
                    contents.put(name, data);
                }

                DataResult<JsonElement> result = fileCodec.encodeStart(JsonOps.INSTANCE, contents);

                if (result.error().isPresent()) {
                    GcaExtension.LOGGER.error("Failed to encode config: {}", result.error().get().message());
                    return;
                }

                JsonElement resultJson = result.result().orElseThrow();
                fileMapper.newName.getParent().toFile().mkdirs();
                Files.writeString(fileMapper.newName, GcaConfig.GSON.toJson(resultJson), StandardCharsets.UTF_8);
            }

            Files.deleteIfExists(fileMapper.oldPath);
        } catch (Exception e) {
            GcaExtension.LOGGER.warn("Failed to update old config: {}", fileMapper.oldPath, e);
        }
    }

    @Nullable
    private static UUID getUUIDByName(Services services, String name, boolean onelineMode) {
        //#if MC < 12109
        GameProfileCache.setUsesAuthentication(false);
        GameProfile gameprofile;
        try {
            gameprofile = services.profileCache().get(name).orElse(null);
        } finally {
            GameProfileCache.setUsesAuthentication(onelineMode);
        }
        if (gameprofile == null) {
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        }
        return gameprofile.getId();
        //#else
        //$$ services.nameToIdCache().resolveOfflineUsers(false);
        //$$ if (!StringUtil.isNullOrEmpty(name) && name.length() <= 16) {
        //$$     Optional<UUID> optional = services.nameToIdCache().get(name).map(NameAndId::id);
        //$$     return optional.orElseGet(() -> UUIDUtil.createOfflinePlayerUUID(name));
        //$$ }
        //$$ try {
        //$$     return UUID.fromString(name);
        //$$ } catch (IllegalArgumentException var5) {
        //$$     return null;
        //$$ }
        //#endif
    }

    private static Optional<CompoundTag> getPlayerData(File file) {
        try {
            return Optional.of(NbtIo.readCompressed(file
                    //#if MC >= 12003
                    .toPath(), NbtAccounter.unlimitedHeap()
                //#endif
            ));
        } catch (Exception var5) {
            return Optional.empty();
        }
    }

    private static BotInfo parseResidentBotInfo(String name, BotActionInfo actions, CompoundTag playerData) {
        //#if MC < 12105
        ListTag posList = playerData.getList("Pos", Tag.TAG_DOUBLE);
        ListTag rotationList = playerData.getList("Rotation", Tag.TAG_FLOAT);
        Vec3 pos = new Vec3(
            Mth.clamp(posList.getDouble(0), -3.0000512E7, 3.0000512E7),
            Mth.clamp(posList.getDouble(1), -2.0E7, 2.0E7),
            Mth.clamp(posList.getDouble(2), -3.0000512E7, 3.0000512E7)
        );
        Vec2 facing = new Vec2(rotationList.getFloat(1), rotationList.getFloat(0));
        GameType mode = playerData.contains("playerGameType", Tag.TAG_ANY_NUMERIC) ?
            GameType.byId(playerData.getInt("playerGameType")) :
            GameType.DEFAULT_MODE;
        boolean flying = playerData.contains("abilities", Tag.TAG_COMPOUND) && playerData.getCompound("abilities").getBoolean("flying");
        //#else
//$$         Vec3 pos = playerData.read("Pos", Vec3.CODEC).orElse(Vec3.ZERO);
//$$         Vec2 facing = playerData.read("Rotation", Vec2.CODEC).map(it -> new Vec2(it.y, it.x)).orElse(Vec2.ZERO);
//$$         GameType mode = playerData.read("playerGameType", GameType.LEGACY_ID_CODEC).orElse(GameType.DEFAULT_MODE);
//$$         boolean flying = playerData.getCompound("abilities")
//$$             .flatMap(it -> it.getBoolean("flying"))
//$$             .orElse(false);
        //#endif
        ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, playerData.get("Dimension"))
            .resultOrPartial(GcaExtension.LOGGER::error)
            .orElse(Level.OVERWORLD);
        return new BotInfo(name, "Resident bot imported from old config", pos, facing, dimension, mode, flying, actions);
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
    ) {
    }

    public record DeprecatedLocInfo(
        long id,
        String desc,
        double x,
        double y,
        double z,
        @SerializedName("dim_type") ResourceKey<Level> dimension
    ) {
    }
}
