package dev.dubhe.gugle.carpet.entry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import dev.dubhe.gugle.carpet.config.IComponentNode;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper.fmt;
import static dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper.highlight;
import static dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper.intro;
import static dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper.prefix;
import static dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper.tr;

public record PageInfo<T extends IComponentNode>(int pageSize, int pageNum, int maxPage, int total, List<T> page) {
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
        int total = collection.size();
        int maxPage = total / pageSize + 1;
        if (pageNum > maxPage) {
            context.getSource().sendFailure(Component.literal("No such page %s".formatted(pageNum)));
            return null;
        }
        List<T> page = collection.stream().skip((long) (pageNum - 1) * pageSize).limit(pageSize).toList();
        return new PageInfo<>(pageSize, pageNum, maxPage, total, page);
    }

    public static int getPage(CommandContext<CommandSourceStack> context) {
        try {
            return IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {
            return 1;
        }
    }

    public void sendPageInfo(CommandContext<CommandSourceStack> context, String title, String command, String... args) {
        this.sendPageInfo(context, tr(title), command, args);
    }

    public void sendPageInfo(CommandContext<CommandSourceStack> context, Pair<String, Object[]> titleWithArgs, String command, String... args) {
        Object[] titleArgs = Arrays.stream(titleWithArgs.getRight()).map(ComponentHelper::highlight).toArray();
        this.sendPageInfo(context, tr(titleWithArgs.getLeft(), titleArgs), command, args);
    }

    public void sendPageInfo(CommandContext<CommandSourceStack> context, MutableComponent title, String command, String... args) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        List<Component> components = new ArrayList<>(this.page.size() + 2);

        components.add(intro(fmt("%s (%s)", title, tr("msg.gca.page.total", highlight(this.total)))
            .withStyle(ChatFormatting.WHITE)));

        for (T node : this.page) {
            components.add(node.component(server, args));
        }

        MutableComponent previous = Component.literal("<<<");
        MutableComponent next = Component.literal(">>>");

        if (this.pageNum <= 1) previous.withStyle(ChatFormatting.DARK_GRAY);
        else previous.withStyle(Style.EMPTY
            .applyFormat(ChatFormatting.GREEN)
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.page.previous")))
            .withClickEvent(ComponentUtil.createClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                command + " " + (this.pageNum - 1)
            ))
        );

        if (this.pageNum >= this.maxPage) next.withStyle(ChatFormatting.DARK_GRAY);
        else next.withStyle(Style.EMPTY
            .applyFormat(ChatFormatting.YELLOW)
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tr("msg.gca.page.next")))
            .withClickEvent(ComponentUtil.createClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                command + " " + (this.pageNum + 1)
            ))
        );

        components.add(intro(fmt(
            "%s (%s) %s",
            previous,
            tr("msg.gca.page.footer", highlight(this.pageNum), highlight(this.maxPage)),
            next
        ).withStyle(ChatFormatting.WHITE)));

        components.forEach(it -> source.sendSystemMessage(prefix(it)));
    }

    public void sendMessage(CommandContext<CommandSourceStack> context, Object title, String command, String... args) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        List<Component> components = new ArrayList<>(this.page.size() + 2);
        components.add(ComponentHelper.fmt("======= %s (Page %s/%s) =======", title, this.pageNum, this.maxPage)
            .withStyle(ChatFormatting.YELLOW));
        for (T node : this.page) {
            components.add(node.component(server, args));
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
