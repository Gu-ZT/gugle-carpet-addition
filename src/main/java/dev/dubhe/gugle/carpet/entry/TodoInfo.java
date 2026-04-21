package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IConfigNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;

public record TodoInfo(
    long id,
    String desc,
    boolean success
) implements IConfigNode {
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

    @Override
    public Component component(MinecraftServer server, String... args) {
        Component component = Component.literal(this.desc).withStyle(
            Style.EMPTY
                .withStrikethrough(this.success)
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.name())))
        );
        Component remove = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove todo")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/todo remove %s".formatted(this.id)))
        );
        return Component.literal(this.success ? "☑" : "☐")
            .append(" ").append(component)
            .append(" ").append(this.success ? successComponent() : unsuccessComponent())
            .append(" ").append(remove);
    }

    private Component successComponent() {
        return Component.literal("[✔]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Make todo done")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/todo success %s".formatted(this.id)))
        );
    }

    private Component unsuccessComponent() {
        return Component.literal("[❌]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Make todo undone")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/todo success %s false".formatted(this.id)))
        );
    }

}
