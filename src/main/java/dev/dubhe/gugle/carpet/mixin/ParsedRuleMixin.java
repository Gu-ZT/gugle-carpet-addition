package dev.dubhe.gugle.carpet.mixin;

import carpet.settings.ParsedRule;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dubhe.gugle.carpet.GcaValidators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@SuppressWarnings("removal")
@Mixin(value = ParsedRule.class, remap = false)
public class ParsedRuleMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    private <E> boolean onAdd(List<E> list, E e, Operation<Boolean> original) {
        if (list.stream().anyMatch(it -> it.getClass() == GcaValidators.CommandLevelWithVanilla.class)) {
            return false;
        }
        return original.call(list, e);
    }

}
