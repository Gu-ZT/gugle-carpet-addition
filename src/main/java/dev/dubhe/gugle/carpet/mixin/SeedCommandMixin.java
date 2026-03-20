package dev.dubhe.gugle.carpet.mixin;

import carpet.utils.CommandHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.SeedCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(SeedCommand.class)
public class SeedCommandMixin {
    @WrapOperation(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;", remap = false))
    private static ArgumentBuilder modify(LiteralArgumentBuilder instance, Predicate<CommandSourceStack> predicate, Operation<ArgumentBuilder> original) {
        Predicate<CommandSourceStack> permissionCheck = stack -> "0".equals(GcaSetting.commandSeed) ?
            predicate.test(stack) : CommandHelper.canUseCommand(stack, GcaSetting.commandSeed);
        return original.call(instance, permissionCheck);
    }
}
