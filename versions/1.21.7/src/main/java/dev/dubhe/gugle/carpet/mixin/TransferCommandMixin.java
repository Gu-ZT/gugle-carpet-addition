package dev.dubhe.gugle.carpet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.PermissionCheck;
import net.minecraft.server.commands.TransferCommand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TransferCommand.class)
abstract class TransferCommandMixin {
    @WrapOperation(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;hasPermission(I)Lnet/minecraft/server/commands/PermissionCheck;"))
    private static PermissionCheck<CommandSourceStack> registerPermission(int i, Operation<PermissionCheck<CommandSourceStack>> original) {
        final PermissionCheck<CommandSourceStack> call = original.call(i);
        return new PermissionCheck<>() {
            @Override
            public int requiredLevel() {
                return call.requiredLevel();
            }
            @Override
            public boolean test(CommandSourceStack stack) {
                return GcaSetting.commandTransfer || call.test(stack);
            }
        };
    }
    @WrapOperation(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;argument(Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;", ordinal = 2))
    private static <T> RequiredArgumentBuilder<CommandSourceStack, T> register(String string, ArgumentType<T> argumentType, Operation<RequiredArgumentBuilder<CommandSourceStack, T>> original) {
        return original.call(string, argumentType).requires(Commands.hasPermission(3));
    }
}
