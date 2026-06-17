package dev.dubhe.gugle.carpet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dubhe.gugle.carpet.api.inject.IFakeResident;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoReplaceTool;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerResident;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IFakeResident {

    @Unique
    @Nullable
    private FakePlayerResident gca$Resident = null;
    @Unique
    private final MinecraftServer gca$server = (MinecraftServer) (Object) this;
    @Unique
    private boolean gca$initiating = false;

    @Inject(method = "loadLevel", at = @At("HEAD"))
    public void initResident(CallbackInfo ci) {
        this.gca$Resident = new FakePlayerResident(this.gca$server);
    }

    @Inject(method = "loadLevel", at = @At("RETURN"))
    public void spawnResident(CallbackInfo ci) {
        if (this.gca$Resident != null) {
            this.gca$Resident.load();
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    public void saveResidentPoint1(CallbackInfo ci) {
        FakePlayerAutoReplaceTool.clear();
        if (this.gca$Resident == null) return;
        if (this.gca$server.isSingleplayer()) return;
        this.gca$Resident.save();
    }

    @WrapOperation(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;initServer()Z"))
    private boolean init(MinecraftServer instance, Operation<Boolean> original) {
        this.gca$initiating = true;
        boolean result = original.call(instance);
        this.gca$initiating = false;
        return result;
    }

    @Inject(method = "saveEverything", at = @At("HEAD"))
    public void saveResidentPoint2(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        if (this.gca$Resident != null && !this.gca$initiating) this.gca$Resident.save();
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    @Nullable
    public FakePlayerResident getGCAResident() {
        return this.gca$Resident;
    }
}
