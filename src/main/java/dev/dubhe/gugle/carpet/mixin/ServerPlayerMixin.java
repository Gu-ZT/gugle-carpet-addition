package dev.dubhe.gugle.carpet.mixin;

import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.tools.player.IGcaPlayer;
import dev.dubhe.gugle.carpet.tools.player.PlayerEnderChestContainer;
import dev.dubhe.gugle.carpet.tools.player.PlayerInventoryContainer;
import net.minecraft.server.MinecraftServer;
//#if MC>=12100
import net.minecraft.server.level.ClientInformation;
//#endif
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, priority = 1001)
abstract class ServerPlayerMixin implements IGcaPlayer {
    @Unique
    private final ServerPlayer gca$self = (ServerPlayer) (Object) this;
    @Unique
    private PlayerInventoryContainer gca$playerInventoryContainer = null;
    @Unique
    private PlayerEnderChestContainer gca$playerEnderChestContainer = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    //#if MC<12100
    //$$ private void init(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, CallbackInfo ci) {
    //#else
    private void init(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ClientInformation clientInformation, CallbackInfo ci) {
    //#endif
        this.gca$playerInventoryContainer = new PlayerInventoryContainer(this.gca$self);
        this.gca$playerEnderChestContainer = new PlayerEnderChestContainer(this.gca$self);
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    public PlayerEnderChestContainer getEnderChestContainer() {
        return this.gca$playerEnderChestContainer;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    public PlayerInventoryContainer getInventoryContainer() {
        return this.gca$playerInventoryContainer;
    }
}
