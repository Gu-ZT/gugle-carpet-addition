package dev.dubhe.gugle.carpet.mixin;

import carpet.helpers.EntityPlayerActionPack;
import dev.dubhe.gugle.carpet.tools.player.IGcaPlayer;
import dev.dubhe.gugle.carpet.tools.player.PlayerInventoryContainer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(value = EntityPlayerActionPack.class, remap = false)
public class EntityPlayerActionPackMixin {
    @Shadow
    @Final
    private ServerPlayer player;

    @Inject(method = "start", at = @At("HEAD"))
    private void resetPlayerButtons(EntityPlayerActionPack.ActionType type, EntityPlayerActionPack.Action action, CallbackInfoReturnable<EntityPlayerActionPack> cir) {
        Consumer<PlayerInventoryContainer> consumer = switch (type) {
            case ATTACK -> PlayerInventoryContainer::resetAttackButton;
            case USE -> PlayerInventoryContainer::resetUseButton;
            default -> null;
        };

        if (consumer != null) {
            PlayerInventoryContainer container = ((IGcaPlayer) this.player).getInventoryContainer();
            consumer.accept(container);
        }
    }
}
