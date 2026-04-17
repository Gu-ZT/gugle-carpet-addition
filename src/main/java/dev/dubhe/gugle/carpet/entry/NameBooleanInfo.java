package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IConfigNode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public record NameBooleanInfo(String name, boolean status) implements IConfigNode {
    public static final Codec<NameBooleanInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(NameBooleanInfo::name),
        Codec.BOOL.fieldOf("status").forGetter(NameBooleanInfo::status)
    ).apply(instance, NameBooleanInfo::new));

    @Override
    public Component component(MinecraftServer server) {
        return Component.literal(this.name + ": " + this.status);
    }
}
