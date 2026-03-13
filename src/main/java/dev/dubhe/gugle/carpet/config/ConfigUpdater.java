package dev.dubhe.gugle.carpet.config;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ConfigUpdater {



    public static void tryUpdateOldVersion(LevelStorageSource.LevelStorageAccess access) {





        Path levelPath = access.getLevelPath(LevelResource.ROOT);




    }

    private static void updateMode1(Path path) {
        File from = path.resolve("bot.gca.json").toFile();
        File to = path.resolve("bots.json").toFile();


    }



    private record NameMapper(String oldName, String newName) {
        public static NameMapper of(String name) {
//            return new NameMapper(name + "gca.json", );
            return null;

        }

        public static NameMapper of(String oldName, String newName) {
            return null;
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

    private record BotGroupInfo(
        String name,
        List<String> bots
    ) {
    }
}
