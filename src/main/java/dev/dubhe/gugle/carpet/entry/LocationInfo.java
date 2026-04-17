package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IConfigNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import dev.dubhe.gugle.carpet.util.PosUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record LocationInfo(
    long id,
    String desc,
    Vec3 pos,
    ResourceKey<Level> dimension
) implements IConfigNode {
    public static final Codec<LocationInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("id").forGetter(LocationInfo::id),
        Codec.STRING.fieldOf("desc").forGetter(LocationInfo::desc),
        Vec3.CODEC.fieldOf("pos").forGetter(LocationInfo::pos),
        ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(LocationInfo::dimension)
    ).apply(instance, LocationInfo::new));

    @Override
    public String name() {
        return String.valueOf(this.id);
    }

    @Override
    public Component component(MinecraftServer server) {
        String name = this.name();
        Component component = Component.literal(this.desc).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(name)))
        );
        List<MutableComponent> pos = PosUtil.pos(this.desc, this.pos, this.dimension);
        Component info = Component.literal("[i]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.YELLOW)
                .withHoverEvent(ComponentUtil.createHoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.literal("View loc point information")
                ))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/loc info %s".formatted(name)))
        );
        Component remove = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove loc point")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/loc remove %s".formatted(this.id)))
        );
        return Component.literal("▶ ").append(component)
            .append(" ").append(pos.getFirst())
            .append(" ").append(info)
            .append(" ").append(remove);
    }
}
