package dev.dubhe.gugle.carpet.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dubhe.gugle.carpet.entry.PlayerGameProfileCache;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.IntStream;

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

    public static <T> Optional<T> getOptional(CommandSupplier<T> getter) throws CommandSyntaxException {
        try {
            return Optional.of(getter.get());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static <T> T getArgOrDefault(CommandSupplier<T> getter, Supplier<T> defaultValue) throws CommandSyntaxException {
        try {
            return getter.get();
        } catch (IllegalArgumentException e) {
            return defaultValue.get();
        }
    }

    public static CompletableFuture<Suggestions> suggestRange(SuggestionsBuilder builder, int min, int max) {
        List<String> nums = IntStream.rangeClosed(min, max).mapToObj(Integer::toString).toList();
        return SharedSuggestionProvider.suggest(nums, builder);
    }

    @FunctionalInterface
    public interface CommandSupplier<T> {
        T get() throws CommandSyntaxException;
    }
}
