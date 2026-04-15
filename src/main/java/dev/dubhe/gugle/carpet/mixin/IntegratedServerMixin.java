package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.api.inject.IFakeResident;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerResident;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
    @Inject(method = "halt", at = @At("HEAD"))
    public void saveResident(boolean bl, CallbackInfo ci) {
        FakePlayerResident resident = ((IFakeResident) this).getGCAResident();
        if (resident != null) resident.save();
    }
}
