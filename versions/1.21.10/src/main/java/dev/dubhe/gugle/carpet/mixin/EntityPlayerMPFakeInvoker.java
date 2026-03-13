package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = EntityPlayerMPFake.class, remap = false)
public interface EntityPlayerMPFakeInvoker {
    @Invoker(remap = false)
    static void invokeLoadPlayerData(EntityPlayerMPFake player) {
    }
}
