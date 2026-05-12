package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IConfigNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;

import java.util.List;

import static dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper.tr;

public record BotGroupInfo(String name, List<String> bots) implements IConfigNode {
    public static final Codec<BotGroupInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(BotGroupInfo::name),
        Codec.STRING.listOf().fieldOf("bots").forGetter(BotGroupInfo::bots)
    ).apply(instance, BotGroupInfo::new));

    @Override
    public Component component(MinecraftServer server, String... args) {
        Component name = Component.literal(this.name).withStyle(
            Style.EMPTY
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.name)))
        );
        Component load = Component.literal("[↑]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.group.load")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot group load %s".formatted(this.name)
                ))
        );
        Component remove = Component.literal("[↓]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.group.unload")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot group unload %s".formatted(this.name)
                ))
        );
        Component info = Component.literal("[i]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.group.info")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bot group info %s".formatted(this.name)
                ))
        );
        Component delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.group.remove")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot group remove %s".formatted(this.name)
                ))
        );
        MutableComponent component = Component.literal("▶ ").append(name);
        component.append(" ").append(load);
        component.append(" ").append(remove);
        component.append(" ").append(info);
        return component.append(" ").append(delete);
    }
}
