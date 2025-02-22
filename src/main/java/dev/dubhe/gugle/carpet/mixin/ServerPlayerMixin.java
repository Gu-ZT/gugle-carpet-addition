package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.tools.player.IGcaPlayer;
import dev.dubhe.gugle.carpet.tools.player.PlayerEnderChestContainer;
import dev.dubhe.gugle.carpet.tools.player.PlayerInventoryContainer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ServerPlayer.class, priority = 1001)
abstract class ServerPlayerMixin implements IGcaPlayer {
    @Unique
    private final ServerPlayer gca$self = (ServerPlayer) (Object) this;
    @Unique
    private final PlayerInventoryContainer gca$playerInventoryContainer = new PlayerInventoryContainer(this.gca$self);
    @Unique
    private final PlayerEnderChestContainer gca$playerEnderChestContainer = new PlayerEnderChestContainer(this.gca$self);

    @SuppressWarnings("AddedMixinMembersNamePattern")
    public PlayerEnderChestContainer getEnderChestContainer() {
        return this.gca$playerEnderChestContainer;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    public PlayerInventoryContainer getInventoryContainer() {
        return this.gca$playerInventoryContainer;
    }
}
