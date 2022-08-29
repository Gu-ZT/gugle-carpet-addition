package dev.dubhe.gugle.carpet.mixin;

import com.mojang.datafixers.util.Pair;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.api.Function;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    ServerLevel self = (ServerLevel) (Object) this;

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        long gameTime = self.getLevel().getGameTime();
        List<Pair<Long, Function>> remove = new ArrayList<>();
        for (Pair<Long, Function> pair : GcaExtension.planFunction) {
            if (pair.getFirst() == gameTime) {
                pair.getSecond().accept();
                remove.add(pair);
            } else if (pair.getFirst() < gameTime) {
                remove.add(pair);
            }
        }
        for (Pair<Long, Function> pair : remove) {
            GcaExtension.planFunction.remove(pair);
        }
    }
}
