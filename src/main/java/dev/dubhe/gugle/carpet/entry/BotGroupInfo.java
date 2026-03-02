package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record BotGroupInfo(String name, List<String> bots) implements IWithName {
    public static final Codec<BotGroupInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(BotGroupInfo::name),
        Codec.STRING.listOf().fieldOf("bots").forGetter(BotGroupInfo::bots)
    ).apply(instance, BotGroupInfo::new));
}
