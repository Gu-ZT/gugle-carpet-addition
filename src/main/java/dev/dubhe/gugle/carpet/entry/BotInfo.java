package dev.dubhe.gugle.carpet.entry;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.tools.CustomCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record BotInfo(
    String name,
    String desc,
    Vec3 pos,
    Vec2 facing,
    ResourceKey<Level> dimType,
    GameType mode,
    boolean flying,
    JsonObject actions
) implements IWithName {
    public static final Codec<BotInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(BotInfo::name),
        Codec.STRING.fieldOf("desc").forGetter(BotInfo::desc),
        Vec3.CODEC.fieldOf("pos").forGetter(bot -> bot.pos),
        CustomCodec.VEC2_CODEC.fieldOf("facing").forGetter(bot -> bot.facing),
        ResourceKey.codec(Registries.DIMENSION).fieldOf("dim_type").forGetter(BotInfo::dimType),
        GameType.CODEC.fieldOf("mode").forGetter(BotInfo::mode),
        Codec.BOOL.fieldOf("flying").forGetter(BotInfo::flying),
        CustomCodec.JSON_CODEC.fieldOf("actions").forGetter(bot -> bot.actions)
    ).apply(instance, BotInfo::new));
}
