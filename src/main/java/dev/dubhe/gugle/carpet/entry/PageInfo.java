package dev.dubhe.gugle.carpet.entry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.config.IComponentNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record PageInfo<T extends IComponentNode>(int pageSize, int pageNum, int maxPage, List<T> page) {
    @Nullable
    public static <T extends IComponentNode> PageInfo<T> ofAll(CommandContext<CommandSourceStack> context, Collection<T> collection) {
        return of(context, collection, Math.max(collection.size(), GcaSetting.gcaPageSize));
    }

    @Nullable
    public static <T extends IComponentNode> PageInfo<T> of(CommandContext<CommandSourceStack> context, Collection<T> collection) {
        return of(context, collection, GcaSetting.gcaPageSize);
    }

    @Nullable
    public static <T extends IComponentNode> PageInfo<T> of(CommandContext<CommandSourceStack> context, Collection<T> collection, int pageSize) {
        int pageNum = getPage(context);
        int size = collection.size();
        int maxPage = size / pageSize + 1;
        if (pageNum > maxPage) {
            context.getSource().sendFailure(Component.literal("No such page %s".formatted(pageNum)));
            return null;
        }
        List<T> page = collection.stream().skip((long) (pageNum - 1) * pageSize).limit(pageSize).toList();
        return new PageInfo<>(pageSize, pageNum, maxPage, page);
    }

    public static int getPage(CommandContext<CommandSourceStack> context) {
        try {
            return IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {
            return 1;
        }
    }

    public void sendMessage(CommandContext<CommandSourceStack> context, Object title, String command) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        List<Component> components = new ArrayList<>(this.page.size() + 2);
        components.add(ComponentTranslate.format("======= %s (Page %s/%s) =======", title, this.pageNum, this.pageSize)
            .withStyle(ChatFormatting.YELLOW));
        for (T node : this.page) {
            components.add(node.component(server));
        }
        Component prevPage = this.pageNum <= 1 ?
            Component.literal("<<<").withStyle(ChatFormatting.GRAY) :
            Component.literal("<<<").withStyle(
                Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    command + " " + (this.pageNum - 1)
                ))
            );
        Component nextPage = this.pageNum >= this.maxPage ?
            Component.literal(">>>").withStyle(ChatFormatting.GRAY) :
            Component.literal(">>>").withStyle(
                Style.EMPTY
                .applyFormat(ChatFormatting.GREEN)
                .withClickEvent(ComponentUtil.createClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    command + " " + (this.pageNum + 1)
                ))
            );
        components.add(Component.literal("=======")
            .withStyle(ChatFormatting.YELLOW)
            .append(" ")
            .append(prevPage)
            .append(" ")
            .append(Component.literal("(Page %s/%s)".formatted(this.pageNum, this.maxPage))
                .withStyle(ChatFormatting.YELLOW))
            .append(" ")
            .append(nextPage)
            .append(" ")
            .append(Component.literal("=======").withStyle(ChatFormatting.YELLOW)));

        components.forEach(source::sendSystemMessage);
    }
}
