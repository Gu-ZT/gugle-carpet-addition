package dev.dubhe.gugle.carpet.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class PosUtils {
    public static @NotNull MutableComponent xaero(String desc, double x, double y, double z, @NotNull ResourceKey<Level> dimType) {
        int color = dimType == Level.OVERWORLD ? 10 :
            dimType == Level.NETHER ? 12 :
                dimType == Level.END ? 13 : 11;
        return Component.literal(
            "xaero-waypoint:%s:%s:%.0f:%.0f:%.0f:%d:false:0:Internal-%s-waypoints"
                .formatted(
                    desc,
                    desc.substring(0, 1),
                    x,
                    y,
                    z,
                    color,
                    dimType.location().getPath()
                )
        );
    }

    public static @NotNull @Unmodifiable List<MutableComponent> pos(String desc, double x, double y, double z, @NotNull ResourceKey<Level> dimension) {
        MutableComponent pos = Component.literal("[%.1f, %.1f, %.1f]".formatted(x, y, z)).withStyle(
            Style.EMPTY
                .applyFormat(
                    dimension == Level.OVERWORLD ?
                        ChatFormatting.GREEN :
                        dimension == Level.NETHER ?
                            ChatFormatting.RED :
                            dimension == Level.END ?
                                ChatFormatting.LIGHT_PURPLE :
                                ChatFormatting.AQUA
                )
                .withHoverEvent(ComponentUtils.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(dimension.location().toString())))
        );
        double scale = 0;
        ResourceKey<Level> toDimension = Level.END;
        if (dimension == Level.NETHER) {
            scale = 8;
            toDimension = Level.OVERWORLD;
        } else if (dimension == Level.OVERWORLD) {
            scale = 0.125;
            toDimension = Level.NETHER;
        }
        MutableComponent toPos = Component.literal("[%.1f, %.1f, %.1f]".formatted(x * scale, y, z * scale)).withStyle(
            Style.EMPTY
                .applyFormat(
                    dimension == Level.OVERWORLD ?
                        ChatFormatting.RED :
                        dimension == Level.NETHER ?
                            ChatFormatting.GREEN :
                            ChatFormatting.AQUA
                )
                .withHoverEvent(ComponentUtils.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(toDimension.location().toString())))
        );
        return scale > 0 ?
            List.of(pos, xaero(desc, x, y, z, dimension), toPos, xaero(desc, x * scale, y, z * scale, toDimension)) :
            List.of(pos, xaero(desc, x, y, z, dimension));
    }

    public static @NotNull @Unmodifiable List<MutableComponent> playerPos(@NotNull ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, true, false));
        Vec3 position = player.position();
        ResourceKey<Level> dimension = player.level().dimension();
        String name = player.getGameProfile().getName();
        List<MutableComponent> pos = PosUtils.pos("Shared Location", position.x, position.y, position.z, dimension);
        MutableComponent component = Component.literal("%s at".formatted(name)).append(" ").append(pos.get(0));
        if (pos.size() > 2) component.append("->").append(pos.get(2));
        return List.of(component, pos.get(1));
    }
}
