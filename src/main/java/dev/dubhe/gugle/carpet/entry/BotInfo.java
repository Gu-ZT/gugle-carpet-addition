package dev.dubhe.gugle.carpet.entry;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.config.IConfigNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

//#if MC < 12105
import dev.dubhe.gugle.carpet.tools.CustomCodec;
//#endif

public record BotInfo(
    String name,
    String desc,
    Vec3 pos,
    Vec2 facing,
    ResourceKey<Level> dimension,
    GameType mode,
    boolean flying,
    BotActionInfo actions
) implements IConfigNode {
    public static final Codec<BotInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(BotInfo::name),
        Codec.STRING.fieldOf("desc").forGetter(BotInfo::desc),
        Vec3.CODEC.fieldOf("pos").forGetter(BotInfo::pos),
        //#if MC < 12105
        CustomCodec.VEC2_CODEC.fieldOf("facing").forGetter(BotInfo::facing),
        //#else
        //$$ Vec2.CODEC.fieldOf("facing").forGetter(BotInfo::facing),
        //#endif
        ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(BotInfo::dimension),
        GameType.CODEC.fieldOf("mode").forGetter(BotInfo::mode),
        Codec.BOOL.fieldOf("flying").forGetter(BotInfo::flying),
        BotActionInfo.CODEC.fieldOf("actions").forGetter(BotInfo::actions)
    ).apply(instance, BotInfo::new));

    public static BotInfo create(ServerPlayer player, String desc, boolean saveAction) {
        String name = player.getGameProfile().getName();
        EntityPlayerActionPack actionPack = saveAction ?
            ((ServerPlayerInterface) player).getActionPack() :
            new EntityPlayerActionPack(player);
        return BotInfo.create(name, desc, player, actionPack);
    }

    public static BotInfo create(String name, String desc, ServerPlayer player, EntityPlayerActionPack actionPack) {
        return new BotInfo(
            name,
            desc,
            player.position(),
            player.getRotationVector(),
            player.level().dimension(),
            player.gameMode.getGameModeForPlayer(),
            player.getAbilities().flying,
            BotActionInfo.fromActionPack(actionPack)
        );
    }

    @Override
    public Component component(MinecraftServer server) {
        Component desc = Component.literal(this.desc).withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.name)))
        );
        boolean notOnline = server.getPlayerList().getPlayerByName(this.name) == null;
        Component load = Component.literal("[↑]").withStyle(
            Style.EMPTY
                .applyFormat(notOnline ? ChatFormatting.GREEN : ChatFormatting.GRAY)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Load bot")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/bot load %s".formatted(this.name)))
        );
        Component remove = Component.literal("[↓]").withStyle(
            Style.EMPTY
                .applyFormat(notOnline ? ChatFormatting.GRAY : ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Unload bot")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/player %s kill".formatted(this.name)))
        );
        Component delete = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove bot")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot remove %s".formatted(this.name)
                ))
        );

        return Component.literal("▶ ")
            .withStyle(notOnline ? ChatFormatting.RED : ChatFormatting.GREEN)
            .append(desc).append(" ")
            .append(load).append(" ")
            .append(remove).append(" ")
            .append(delete);
    }
}
