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
    String action,
    boolean startup
) implements IComponentNode {
    public static final Codec<BotExecutorInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("id").forGetter(BotExecutorInfo::id),
        Codec.STRING.fieldOf("desc").forGetter(BotExecutorInfo::desc),
        Codec.STRING.fieldOf("action").forGetter(BotExecutorInfo::action),
        Codec.BOOL.fieldOf("startup").forGetter(BotExecutorInfo::startup)
    ).apply(instance, BotExecutorInfo::new));

    public String command(String name) {
        return "/player %s %s".formatted(name, this.action);
    }

    public BotExecutorInfo withStartup(boolean startup) {
        return new BotExecutorInfo(this.id, this.desc, this.action, startup);
    }

    @Override
    public Component component(MinecraftServer server, String... args) {
        if (args.length == 0) {
            return Component.literal("Error: No player name provided").withStyle(ChatFormatting.RED);
        }
        String name = args[0];
        String command = this.command(name);
        Component tooltip = Component.literal("")
            .append(Component.literal(String.valueOf(this.id)).withStyle(ChatFormatting.AQUA))
            .append(Component.literal("\n" + command));
        Component desc = Component.literal(this.desc).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))
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
        Component mark = Component.literal("▶").withStyle(
            this.startup ? Style.EMPTY.applyFormat(ChatFormatting.GOLD) : Style.EMPTY
        );

        return Component.literal("")
            .append(mark).append(" ")
            .append(desc).append(" ")
            .append(execute).append(" ")
            .append(delete);
    }
}
