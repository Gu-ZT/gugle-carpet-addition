package dev.dubhe.gugle.carpet.entry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public record PageInfo<T>(int pageSize, int pageNum, int maxPage, List<T> page) {
    @Nullable
    public static <T> PageInfo<T> of(CommandContext<CommandSourceStack> context, Collection<T> collection) {
        final int pageSize = GcaSetting.gcaPageSize;
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
}
