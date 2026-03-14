package dev.dubhe.gugle.carpet.entry;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.GcaExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record WelcomeInfo(List<String> messages, Map<String, MessageArg> args) implements IWithName {
    public static final String KEY = "info";
    public static final Codec<WelcomeInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().fieldOf("message").forGetter(WelcomeInfo::messages),
        Codec.unboundedMap(Codec.STRING, MessageArg.CODEC).fieldOf("args").forGetter(WelcomeInfo::args)
    ).apply(instance, WelcomeInfo::new));

    @Override
    public String name() {
        return KEY;
    }

    public static WelcomeInfo defaultInfo() {
        MessageArg arg = new MessageArg(GcaExtension.id("player"), Optional.empty());
        return new WelcomeInfo(List.of("{%player%}, welcome!"), Map.of("player", arg));
    }

    public record MessageArg(ResourceLocation type, Optional<JsonElement> data) {
        private static final Codec<MessageArg> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(MessageArg::type),
            ExtraCodecs.JSON.optionalFieldOf("data").forGetter(MessageArg::data)
        ).apply(instance, MessageArg::new));
        public static final Codec<MessageArg> CODEC = Codec.either(ResourceLocation.CODEC, OBJECT_CODEC).xmap(
            either -> either.map(type -> new MessageArg(type, Optional.empty()), arg -> arg),
            arg -> arg.data().isPresent() ? Either.right(arg) : Either.left(arg.type())
        );
    }

    @FunctionalInterface
    public interface IMessageReplacer {
        Component getMessage(MinecraftServer server, ServerPlayer player, @org.jetbrains.annotations.Nullable JsonElement args) throws Exception;
    }


}
