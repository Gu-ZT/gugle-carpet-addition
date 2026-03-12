package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.tools.player.FakePlayerResident;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Unique
    @Nullable
    private FakePlayerResident gca$Resident = null;

    @Inject(method = "loadLevel", at = @At("HEAD"))
    public void initResident(CallbackInfo ci) {
        this.gca$Resident = new FakePlayerResident((MinecraftServer) (Object) this);
    }

    @Inject(method = "loadLevel", at = @At("RETURN"))
    public void spawnResident(CallbackInfo ci) {
        if (this.gca$Resident != null) {
            this.gca$Resident.load();
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    public void saveResidentPoint1(CallbackInfo ci) {
        if (this.gca$Resident != null) this.gca$Resident.save();
    }

    @Inject(method = "saveEverything", at = @At("HEAD"))
    public void saveResidentPoint2(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        if (this.gca$Resident != null) this.gca$Resident.save();
    }
}
