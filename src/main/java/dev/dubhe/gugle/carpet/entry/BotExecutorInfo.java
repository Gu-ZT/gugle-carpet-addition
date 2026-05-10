package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IComponentNode;
import dev.dubhe.gugle.carpet.config.IIdNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;

import static dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper.tr;

public record BotExecutorInfo(
    long id,
    String desc,
    String action,
    boolean startup
) implements IComponentNode, IIdNode {
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
        Component execute = Component.literal("[>]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.action.execute")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, command))
        );
        Component delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.action.remove")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot action %s remove %s".formatted(name, this.id)
                ))
        );
        MutableComponent mark = Component.literal("▶");
        if (this.startup) {
            mark.withStyle(Style.EMPTY
                .withColor(ChatFormatting.GOLD)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.action.startup")))
            );
        }

        return Component.literal("")
            .append(mark).append(" ")
            .append(desc).append(" ")
            .append(execute).append(" ")
            .append(delete);
    }
}
