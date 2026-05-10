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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//#if MC < 12105
import dev.dubhe.gugle.carpet.tools.CustomCodec;

import static dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper.tr;
//#endif

public record BotInfo(
    String name,
    String desc,
    Vec3 pos,
    Vec2 facing,
    ResourceKey<Level> dimension,
    GameType mode,
    boolean flying,
    BotActionInfo actions,
    List<BotExecutorInfo> executors
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
        BotActionInfo.CODEC.fieldOf("actions").forGetter(BotInfo::actions),
        BotExecutorInfo.CODEC.listOf().optionalFieldOf("executors", List.of()).forGetter(BotInfo::executors)
    ).apply(instance, BotInfo::new));

    public List<BotExecutorInfo> getStartups() {
        return this.executors.stream().filter(BotExecutorInfo::startup).toList();
    }

    public static BotInfo create(ServerPlayer player, String desc, boolean saveAction) {
        String name = player.getGameProfile().getName();
        EntityPlayerActionPack actionPack = saveAction ? ((ServerPlayerInterface) player).getActionPack() : null;
        return BotInfo.create(name, desc, player, actionPack);
    }

    public static BotInfo create(String name, String desc, ServerPlayer player, @Nullable EntityPlayerActionPack actionPack) {
        return new BotInfo(
            name,
            desc,
            player.position(),
            player.getRotationVector(),
            player.level().dimension(),
            player.gameMode.getGameModeForPlayer(),
            player.getAbilities().flying,
            BotActionInfo.fromActionPack(actionPack),
            List.of()
        );
    }

    public BotInfo withExecutors(@Nullable BotInfo bot) {
        if (bot == null) return this;
        return this.withExecutors(bot.executors);
    }

    public BotInfo withExecutors(List<BotExecutorInfo> executors) {
        return new BotInfo(
            this.name,
            this.desc,
            this.pos,
            this.facing,
            this.dimension,
            this.mode,
            this.flying,
            this.actions,
            executors
        );
    }

    @Override
    public Component component(MinecraftServer server, String... args) {
        boolean showAction = args.length > 0 && "true".equals(args[0]);
        Component desc = Component.literal(this.desc).withStyle(
            Style.EMPTY.withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.name)))
        );

        boolean online = server.getPlayerList().getPlayerByName(this.name) != null;

        Component spawn = Component.literal("[↑]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.load")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/bot load %s".formatted(this.name)))
        );
        Component kill = Component.literal("[↓]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.unload")))
                .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/player %s kill".formatted(this.name)))
        );
        Component remove = Component.literal("[\uD83D\uDDD1]").withStyle(
            Style.EMPTY
                .applyFormat(ChatFormatting.RED)
                .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.remove")))
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/bot remove %s".formatted(this.name)
                ))
        );

        MutableComponent result = Component.literal("")
            .append(Component.literal("▶").withStyle(
                Style.EMPTY
                    .withColor(online ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr(
                        online ? "multiplayer.status.online" : "gui.socialInteractions.status_offline"
                    )))
            ))
            .append(" ").append(desc)
            .append(" ").append(spawn)
            .append(" ").append(kill);

        if (showAction) {
            Component action = Component.literal("[⚙]").withStyle(
                Style.EMPTY
                    .applyFormat(ChatFormatting.GRAY)
                    .withHoverEvent(ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.bot.actions", this.executors.size())))
                    .withClickEvent(ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/bot action %s".formatted(this.name)))
            );
            result.append(" ").append(action);
        }

        result.append(" ").append(remove);

        return result;
    }
}
