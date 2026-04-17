package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IComponentNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;

public record BotExecutorInfo(
    long id,
    String desc,
    String action
) implements IComponentNode {
    public static final Codec<BotExecutorInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("id").forGetter(BotExecutorInfo::id),
        Codec.STRING.fieldOf("desc").forGetter(BotExecutorInfo::desc),
        Codec.STRING.fieldOf("action").forGetter(BotExecutorInfo::action)
    ).apply(instance, BotExecutorInfo::new));

    @Override
    public Component component(MinecraftServer server, String... args) {
        String name = args[0];
        String command = "/player %s %s".formatted(name, this.desc);
        Component desc = Component.literal(this.desc).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(command)))
        );
        Component execute = Component.literal("[▶]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Execute action")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, command))
        );
        Component delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove action")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot action %s remove %s".formatted(name, this.id)
                ))
        );

        return Component.literal("▶ ")
            .append(desc).append(" ")
            .append(execute).append(" ")
            .append(delete);
    }
}
