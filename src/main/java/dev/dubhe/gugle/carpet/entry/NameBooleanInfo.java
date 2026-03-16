package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NameBooleanInfo(String name, boolean status) implements IWithName {
    public static final Codec<NameBooleanInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(NameBooleanInfo::name),
        Codec.BOOL.fieldOf("status").forGetter(NameBooleanInfo::status)
    ).apply(instance, NameBooleanInfo::new));
}
