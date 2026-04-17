package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IComponentNode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public record BotExecuterInfo(
    long id,
    String desc,
    String action
) implements IComponentNode {
    public static final Codec<BotExecuterInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("id").forGetter(BotExecuterInfo::id),
        Codec.STRING.fieldOf("desc").forGetter(BotExecuterInfo::desc),
        Codec.STRING.fieldOf("action").forGetter(BotExecuterInfo::action)
    ).apply(instance, BotExecuterInfo::new));

    @Override
    public Component component(MinecraftServer server) {
        return null;
    }
}
