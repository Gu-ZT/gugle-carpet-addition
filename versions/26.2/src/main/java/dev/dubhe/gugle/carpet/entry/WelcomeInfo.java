package dev.dubhe.gugle.carpet.entry;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.config.IConfigNode;
import dev.dubhe.gugle.carpet.config.updater.serializer.ChatFormattingSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record WelcomeInfo(List<String> messages, Map<String, MessageArg> args) implements IConfigNode {
    public static final String KEY = "info";
    public static final Codec<WelcomeInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().fieldOf("message").forGetter(WelcomeInfo::messages),
        Codec.unboundedMap(Codec.STRING, MessageArg.CODEC).fieldOf("args").forGetter(WelcomeInfo::args)
    ).apply(instance, WelcomeInfo::new));

    @Override
    public String name() {
        return KEY;
    }

    @Override
    public Component component(MinecraftServer server, String... args) {
        return Component.literal(KEY);
    }

    public static WelcomeInfo defaultInfo() {
        MessageArg arg = new MessageArg(GcaExtension.id("player"));
        return new WelcomeInfo(List.of("{%player%}, welcome!"), Map.of("player", arg));
    }

    public record MessageArg(Identifier type, Optional<JsonElement> data, ChatFormatting style) {
        private static final Codec<MessageArg> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("type").forGetter(MessageArg::type),
            ExtraCodecs.JSON.optionalFieldOf("data").forGetter(MessageArg::data),
            ChatFormattingSerializer.CODEC.optionalFieldOf("color", ChatFormatting.GOLD).forGetter(MessageArg::style)
        ).apply(instance, MessageArg::new));
        public static final Codec<MessageArg> CODEC = Codec.either(Identifier.CODEC, OBJECT_CODEC).xmap(
            either -> either.map(MessageArg::new, arg -> arg),
            arg -> arg.data.isEmpty() && arg.style == ChatFormatting.GOLD ? Either.left(arg.type()) : Either.right(arg)
        );

        public MessageArg(Identifier type) {
            this(type, Optional.empty(), ChatFormatting.GOLD);
        }
    }

    @FunctionalInterface
    public interface IMessageReplacer {
        MutableComponent getMessage(
            MinecraftServer server,
            ServerPlayer player,
            @org.jetbrains.annotations.Nullable JsonElement args
        ) throws Exception;
    }


}
