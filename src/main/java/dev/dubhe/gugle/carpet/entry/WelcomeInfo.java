package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.tools.WelcomeMessage;

import java.util.List;
import java.util.Map;

public record WelcomeInfo(List<String> messages, Map<String, WelcomeMessage.MessageData> args) implements IWithName {
    public static final Codec<WelcomeInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().optionalFieldOf("messages", List.of("{%player%}, welcome!")).forGetter(WelcomeInfo::messages),
        Codec.unboundedMap(Codec.STRING, WelcomeMessage.MessageData.CODEC)
            .optionalFieldOf("args", Map.of("player", new WelcomeMessage.MessageData()))
            .forGetter(WelcomeInfo::args)
    ).apply(instance, WelcomeInfo::new));

    @Override
    public String name() {
        return "data";
    }

    public WelcomeMessage.MessageData getArg(String key) {
        return this.args.getOrDefault(key, new WelcomeMessage.MessageData());
    }

}
