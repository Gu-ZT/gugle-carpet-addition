package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IConfigNode;
import dev.dubhe.gugle.carpet.config.IIdNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;

import static dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper.tr;

public record TodoInfo(
    long id,
    String desc,
    boolean success
) implements IConfigNode, IIdNode {
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
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.name())))
        );
        Component remove = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove todo")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/todo remove %s".formatted(this.id)))
        );

        Pair<Component, Component> components = this.success ? successComponent() : unsuccessComponent();

        return Component.literal("")
            .append(components.getLeft())
            .append(" ").append(component)
            .append(" ").append(components.getRight())
            .append(" ").append(remove);
    }

    private Pair<Component, Component> successComponent() {
        Component head = Component.literal("☑").withStyle(
            Style.EMPTY.withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.todo.status.completed")))
        );

        Component operate = Component.literal("[❌]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.todo.status.undone")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/todo success %s false".formatted(this.id)))
        );
        return Pair.of(head, operate);
    }

    private Pair<Component, Component> unsuccessComponent() {
        Component head = Component.literal("☐").withStyle(
            Style.EMPTY.withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.todo.status.incompleted")))
        );

        Component operate = Component.literal("[✔]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.todo.status.done")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/todo success %s".formatted(this.id)))
        );
        return Pair.of(head, operate);
    }

}
