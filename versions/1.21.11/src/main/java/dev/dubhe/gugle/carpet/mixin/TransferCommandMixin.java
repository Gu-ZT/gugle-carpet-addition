package dev.dubhe.gugle.carpet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.TransferCommand;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(TransferCommand.class)
abstract class TransferCommandMixin {
    @WrapOperation(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;"
        )
    )
    private static <S extends CommandSourceStack, T extends ArgumentBuilder<S, T>> T registerPermission(
        LiteralArgumentBuilder<CommandSourceStack> instance,
        Predicate<CommandSourceStack> predicate,
        @NonNull Operation<T> original
    ) {
        Predicate<CommandSourceStack> predicate1 = (stack) -> GcaSetting.commandTransfer || predicate.test(stack);
        return original.call(instance, predicate1);
    }

    @WrapOperation(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/Commands;argument("
                     + "Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;"
                     + ")Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;",
            ordinal = 2
        )
    )
    private static <T> @NotNull RequiredArgumentBuilder<CommandSourceStack, T> register(
        String string,
        ArgumentType<T> argumentType,
        @NotNull Operation<RequiredArgumentBuilder<CommandSourceStack, T>> original
    ) {
        return original.call(string, argumentType).requires(Commands.hasPermission(Commands.LEVEL_ADMINS));
    }
}
