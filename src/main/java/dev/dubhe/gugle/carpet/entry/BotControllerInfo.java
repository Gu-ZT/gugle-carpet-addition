package dev.dubhe.gugle.carpet.entry;

import carpet.helpers.EntityPlayerActionPack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IComponentNode;
import dev.dubhe.gugle.carpet.config.IConfigNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record BotControllerInfo(
    String bot,
    Map<Integer, ControllerNode> controllers
) implements IConfigNode {
    public static final Codec<BotControllerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("bot").forGetter(BotControllerInfo::bot),
        ControllerNode.CODEC.listOf().fieldOf("controllers").xmap(
            it -> it.stream().collect(Collectors.toUnmodifiableMap(ControllerNode::slot, Function.identity())),
            it -> it.values().stream().toList()
        ).forGetter(BotControllerInfo::controllers)
    ).apply(instance, BotControllerInfo::new));

    @Override
    public String name() {
        return this.bot;
    }

    @Override
    public Component component(MinecraftServer server, String... args) {
        return Component.empty();
    }

    public record ControllerNode(int slot, String desc, String command) implements IComponentNode {
        public static final Codec<ControllerNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("slot").forGetter(ControllerNode::slot),
            Codec.STRING.fieldOf("desc").forGetter(ControllerNode::desc),
            Codec.STRING.fieldOf("command").forGetter(ControllerNode::command)
        ).apply(instance, ControllerNode::new));

        public static final ControllerNode EMPTY = new ControllerNode(-1, "EMPTY", "");

        @Override
        public Component component(MinecraftServer server, String... args) {
            return Component.literal(String.format("[#%s]", this.slot)).withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" "))
                .append(Component.literal(this.desc).withStyle(
                    Style.EMPTY.withHoverEvent(ComponentUtil.createHoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.literal("/player xxx " + this.command)
                    ))
                ));
        }

        public void execute(ServerPlayer player, EntityPlayerActionPack ap) {
        }

    }

}
