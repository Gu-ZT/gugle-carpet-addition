package dev.dubhe.gugle.carpet.entry;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Optional;

public record WelcomeInfo(List<String> messages, Optional<JsonElement> args) implements IWithName {
    public static final String KEY = "info";
    public static final Codec<WelcomeInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().fieldOf("messages").forGetter(WelcomeInfo::messages),
        ExtraCodecs.JSON.optionalFieldOf("args").forGetter(WelcomeInfo::args)
    ).apply(instance, WelcomeInfo::new));

    @Override
    public String name() {
        return KEY;
    }

    public static WelcomeInfo defaultInfo() {
        return new WelcomeInfo(List.of("{%player%}, welcome!"), Optional.empty());
    }

    @FunctionalInterface
    public interface IMessageReplacer {
        Component getMessage(MinecraftServer server, ServerPlayer player, @org.jetbrains.annotations.Nullable JsonElement args) throws Exception;
    }

}
