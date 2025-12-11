package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWaypointManager.class)
abstract class LocatorBarMixin {
    @Inject(
        method = "trackWaypoint(Lnet/minecraft/world/waypoints/WaypointTransmitter;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void trackWaypoint(WaypointTransmitter waypointTransmitter, CallbackInfo ci) {
        if (
            !GcaSetting.fakePlayerLocatorBar
            && waypointTransmitter instanceof ServerPlayer serverPlayer
            && serverPlayer instanceof EntityPlayerMPFake
        ) {
            ci.cancel();
        }
    }

    @Inject(
        method = "addPlayer",
        at = @At("HEAD"),
        cancellable = true
    )
    private void addPlayer(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (!GcaSetting.fakePlayerLocatorBar && serverPlayer instanceof EntityPlayerMPFake) {
            ci.cancel();
        }
    }

    @Inject(
        method = "updatePlayer",
        at = @At("HEAD"),
        cancellable = true
    )
    private void updatePlayer(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (!GcaSetting.fakePlayerLocatorBar && serverPlayer instanceof EntityPlayerMPFake) {
            ci.cancel();
        }
    }

    @Inject(
        method = "isLocatorBarEnabledFor",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void isLocatorBarEnabledFor(ServerPlayer serverPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (!GcaSetting.fakePlayerLocatorBar && serverPlayer instanceof EntityPlayerMPFake) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "createConnection",
        at = @At("HEAD"),
        cancellable = true
    )
    private void createConnection(ServerPlayer serverPlayer, WaypointTransmitter waypointTransmitter, CallbackInfo ci) {
        if (!GcaSetting.fakePlayerLocatorBar && serverPlayer instanceof EntityPlayerMPFake) {
            ci.cancel();
        }
    }

    @Inject(
        method = "updateConnection",
        at = @At("HEAD"),
        cancellable = true
    )
    private void updateConnection(
        ServerPlayer serverPlayer,
        WaypointTransmitter waypointTransmitter,
        WaypointTransmitter.Connection connection,
        CallbackInfo ci
    ) {
        if (!GcaSetting.fakePlayerLocatorBar && serverPlayer instanceof EntityPlayerMPFake) {
            ci.cancel();
        }
    }
}
