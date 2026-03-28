package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record BotRecordActionInfo(String name, List<BotExecActionInfo> ofActions) implements IWithName {
    public static final Codec<BotRecordActionInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(BotRecordActionInfo::name),
        BotExecActionInfo.CODEC.listOf().fieldOf("actions").forGetter(BotRecordActionInfo::ofActions)
    ).apply(instance, BotRecordActionInfo::new));

    public record BotExecActionInfo(long id, String desc, String action) {
        public static final Codec<BotExecActionInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("id").forGetter(BotExecActionInfo::id),
            Codec.STRING.fieldOf("desc").forGetter(BotExecActionInfo::desc),
            Codec.STRING.fieldOf("action").forGetter(BotExecActionInfo::action)
        ).apply(instance, BotExecActionInfo::new));
    }

    public static BotRecordActionInfo create(String name) {
        return new BotRecordActionInfo(name, List.of());
    }

    public BotRecordActionInfo ofActions(List<BotExecActionInfo> actions) {
        return new BotRecordActionInfo(this.name, actions);
    }
}
