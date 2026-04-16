package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BotExecuterInfo(
    long id,
    String bot,
    String desc,
    String action
) implements IWithName {
    public static final Codec<BotExecuterInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("id").forGetter(BotExecuterInfo::id),
        Codec.STRING.fieldOf("bot").forGetter(BotExecuterInfo::bot),
        Codec.STRING.fieldOf("desc").forGetter(BotExecuterInfo::desc),
        Codec.STRING.fieldOf("action").forGetter(BotExecuterInfo::action)
    ).apply(instance, BotExecuterInfo::new));


    @Override
    public String name() {
        return String.valueOf(this.id);
    }
}
