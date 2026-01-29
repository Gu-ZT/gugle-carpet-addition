package dev.dubhe.gugle.carpet.mixin;

import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TransferCommand;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(TransferCommand.class)
abstract class TransferCommandMixin {
    @WrapOperation(method = "method_56524", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;hasPermission(I)Z"))
    private static boolean registerPermission(CommandSourceStack instance, int i, Operation<Boolean> original) {
        return GcaSetting.commandTransfer || original.call(instance, i);
    }

    @WrapOperation(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;argument(Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;", ordinal = 2))
    private static <T> RequiredArgumentBuilder<CommandSourceStack, T> register(String string, ArgumentType<T> argumentType, Operation<RequiredArgumentBuilder<CommandSourceStack, T>> original) {
        return original.call(string, argumentType).requires(stack -> stack.hasPermission(3));
    }
}
