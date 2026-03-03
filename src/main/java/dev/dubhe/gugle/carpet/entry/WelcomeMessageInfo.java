package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.tools.WelcomeMessage;

import java.util.List;
import java.util.Map;

public record WelcomeMessageInfo(List<String> messages, Map<String, WelcomeMessage.MessageData> args) implements IWithName {
    public static final Codec<WelcomeMessageInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().optionalFieldOf("messages", List.of("{%player%}, welcome!")).forGetter(WelcomeMessageInfo::messages),
        Codec.unboundedMap(Codec.STRING, WelcomeMessage.MessageData.CODEC)
            .optionalFieldOf("args", Map.of("player", new WelcomeMessage.MessageData()))
            .forGetter(WelcomeMessageInfo::args)
    ).apply(instance, WelcomeMessageInfo::new));

    @Override
    public String name() {
        return "data";
    }

    public WelcomeMessage.MessageData getArg(String key) {
        return this.args.getOrDefault(key, new WelcomeMessage.MessageData());
    }

}
