package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.tools.FastPingFriend;
import dev.dubhe.gugle.carpet.tools.SimpleInGameCalculator;
import dev.dubhe.gugle.carpet.entry.TriConsumer;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerAutoRespawn;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#if MC < 12002
//$$ import java.util.concurrent.CompletableFuture;
//$$ import java.util.concurrent.ExecutionException;
//#endif
//#if MC > 12005
import net.minecraft.network.DisconnectionDetails;
//#endif

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public abstract ServerPlayer getPlayer();

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    private void onDisconnect(
        //#if MC > 12005
        DisconnectionDetails
        //#else
        //$$ Component
        //#endif
            details, CallbackInfo ci) {
        if (this.getPlayer() instanceof EntityPlayerMPFake fakePlayer) {
            FakePlayerAutoRespawn.tryRespawn(fakePlayer);
        }
    }

    @Inject(method =
        //#if MC < 260000
        "method_45064"
        //#else
        //$$ "lambda$handleChat$1"
        //#endif
        , at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;)V", shift = At.Shift.AFTER))
    //#if MC>=12002
    private void handleChat(PlayerChatMessage playerChatMessage, Component component, FilteredText filteredText, CallbackInfo ci) {
        this.gca$handleChat(GcaSetting.simpleInGameCalculator, "==", component, (server, player, msg) -> SimpleInGameCalculator.handleChat(server, msg));
        this.gca$handleChat(GcaSetting.fastPingFriend, "@ ", component, FastPingFriend::handleChat);
        this.gca$handleChat(GcaSetting.fastPingFriend, "@@ ", component, FastPingFriend::handleChatUrgent);
    }
    //#else
    //$$ private void handleChat(PlayerChatMessage playerChatMessage, CompletableFuture<Component> completableFuture, CompletableFuture<FilteredText> completableFuture2, Void void_, CallbackInfo ci) throws ExecutionException, InterruptedException {
    //$$     this.gca$handleChat(GcaSetting.simpleInGameCalculator, "==", completableFuture.get(), (server, player, msg) -> SimpleInGameCalculator.handleChat(server, msg));
    //$$     this.gca$handleChat(GcaSetting.fastPingFriend, "@ ", completableFuture.get(), FastPingFriend::handleChat);
    //$$     this.gca$handleChat(GcaSetting.fastPingFriend, "@@ ", completableFuture.get(), FastPingFriend::handleChatUrgent);
    //$$ }
    //#endif

    @Unique
    private void gca$handleChat(boolean rule, String prefix, Component component, TriConsumer<MinecraftServer, ServerPlayer, String> handle) {
        if (!rule) return;
        String string = component.getString();
        if (!string.startsWith(prefix)) return;
        string = string.substring(prefix.length());
        ServerPlayer player = this.getPlayer();
        handle.accept(player.level().getServer(), player, string);
    }
}
