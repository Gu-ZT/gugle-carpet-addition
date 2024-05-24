package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.tools.FakePlayerEnderChestContainer;
import dev.dubhe.gugle.carpet.tools.FakePlayerInventoryContainer;
import dev.dubhe.gugle.carpet.tools.FakePlayerInventoryMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Unique
    Player gca$self = (Player) (Object) this;

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        if (gca$self instanceof ServerPlayer serverPlayer &&
            serverPlayer instanceof EntityPlayerMPFake && serverPlayer.isAlive()) {
            Map.Entry<FakePlayerInventoryContainer, FakePlayerEnderChestContainer> entry = GcaExtension.fakePlayerInventoryContainerMap.get(gca$self);
            entry.getKey().tick();
            entry.getValue().tick();
        }
    }

    @Redirect(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult interactOn(Entity entity, Player player, InteractionHand hand) {
        if (!(entity instanceof EntityPlayerMPFake fakePlayer)) return entity.interact(player, hand);
        SimpleMenuProvider provider = null;
        if (player.isShiftKeyDown()) {
            if (GcaSetting.openFakePlayerEnderChest) {
                provider = new SimpleMenuProvider(
                    (i, inventory, p) -> ChestMenu.sixRows(
                        i, inventory,
                        GcaExtension.fakePlayerInventoryContainerMap.get(fakePlayer).getValue()
                    ),
                    ComponentTranslate.trans("gca.player.ender_chest", fakePlayer.getDisplayName())
                );
            } else {
                provider = new SimpleMenuProvider(
                    (i, inventory, p) -> ChestMenu.threeRows(
                        i, inventory,
                        GcaExtension.fakePlayerInventoryContainerMap.get(fakePlayer).getValue()
                    ),
                    ComponentTranslate.trans("gca.player.other_controller", fakePlayer.getDisplayName())
                );
            }
        } else if (GcaSetting.openFakePlayerInventory) {
            provider = new SimpleMenuProvider(
                (i, inventory, p) -> new FakePlayerInventoryMenu(
                    i, inventory,
                    GcaExtension.fakePlayerInventoryContainerMap.get(fakePlayer).getKey()
                ),
                ComponentTranslate.trans("gca.player.inventory", fakePlayer.getDisplayName())
            );
        }
        if (provider != null) {
            player.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }
}
