package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TodoInfo(
    long id,
    String desc,
    boolean success
) implements IWithName {
    public static final Codec<TodoInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("id").forGetter(TodoInfo::id),
        Codec.STRING.fieldOf("desc").forGetter(TodoInfo::desc),
        Codec.BOOL.fieldOf("success").forGetter(TodoInfo::success)
    ).apply(instance, TodoInfo::new));

    @Override
    public String name() {
        return String.valueOf(this.id);
    }

    public TodoInfo ofSuccess(boolean success) {
        return new TodoInfo(this.id, this.desc, success);
    }
}
