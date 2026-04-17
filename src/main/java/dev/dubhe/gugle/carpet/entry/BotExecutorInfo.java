package dev.dubhe.gugle.carpet.entry;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.config.IComponentNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

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

    public boolean execute(CommandSourceStack source, BotInfo bot) throws CommandSyntaxException {
        MinecraftServer server = source.getServer();
        ServerPlayer player = server.getPlayerList().getPlayerByName(bot.name());
        if (player == null) {
            source.sendFailure(ComponentTranslate.formatNames("Bot %s is not online.", bot.name()));
            return false;
        }
        return server.getCommands().getDispatcher().execute(
            "player %s %s".formatted(bot.name(), this.action),
            server.createCommandSourceStack()
        ) > 0;
    }

    @Override
    public Component component(MinecraftServer server) {
        return Component.literal(this.desc);
    }
}
