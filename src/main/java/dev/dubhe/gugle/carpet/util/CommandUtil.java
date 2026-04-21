package dev.dubhe.gugle.carpet.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.dubhe.gugle.carpet.entry.PlayerGameProfileCache;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;

import java.util.List;
import java.util.function.Supplier;

public class CommandUtil {

    public static List<PlayerGameProfileCache> parseGameProfiles(
        CommandContext<CommandSourceStack> context,
        String name
    ) throws CommandSyntaxException {
        return GameProfileArgument.getGameProfiles(context, name)
            .stream()
            .map(PlayerGameProfileCache::of)
            .toList();
    }

    public static <T> T getArgOrDefault(CommandSupplier<T> getter, Supplier<T> defaultValue) throws CommandSyntaxException {
        try {
            return getter.get();
        } catch (IllegalArgumentException e) {
            return defaultValue.get();
        }
    }

    @FunctionalInterface
    public interface CommandSupplier<T> {
        T get() throws CommandSyntaxException;
    }
}
