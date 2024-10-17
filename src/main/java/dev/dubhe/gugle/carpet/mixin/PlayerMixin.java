package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.tools.ClientUtils;
import dev.dubhe.gugle.carpet.tools.FakePlayerEnderChestContainer;
import dev.dubhe.gugle.carpet.tools.FakePlayerInventoryContainer;
import dev.dubhe.gugle.carpet.tools.FakePlayerInventoryMenu;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Unique
    private final Player gca$self = (Player) (Object) this;

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        if (gca$self instanceof EntityPlayerMPFake fakePlayer && fakePlayer.isAlive()) {
            Map.Entry<FakePlayerInventoryContainer, FakePlayerEnderChestContainer> entry
                    = GcaExtension.fakePlayerInventoryContainerMap.get(gca$self);
            entry.getKey().tick();
            entry.getValue().tick();
        }
    }

    @WrapOperation(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult interactOn(Entity entity, Player player, InteractionHand hand, Operation<InteractionResult> original) {
        if (player.level().isClientSide()) {
            // 客户端在交互前要先判断一下当前交互的实体是不是玩家，这用来防止意外的使用物品功能
            if (entity instanceof Player && ClientUtils.isFakePlayer(player)) {
                return InteractionResult.CONSUME;
            }
        } else {
            if ((GcaSetting.openFakePlayerInventory || GcaSetting.openFakePlayerEnderChest) && entity instanceof EntityPlayerMPFake fakePlayer) {
                // 打开物品栏
                InteractionResult result = this.openInventory(player, fakePlayer);
                if (result != InteractionResult.PASS) {
                    player.stopUsingItem();
                    return result;
                }
            }
        }
        return original.call(entity, player, hand);
    }

    @Unique
    private InteractionResult openInventory(Player player, EntityPlayerMPFake fakePlayer) {
        SimpleMenuProvider provider;
        if (player.isShiftKeyDown()) {
            // 打开末影箱
            if (GcaSetting.openFakePlayerEnderChest) {
                provider = new SimpleMenuProvider(
                        (i, inventory, p) -> ChestMenu.sixRows(
                                i, inventory,
                                GcaExtension.fakePlayerInventoryContainerMap.get(fakePlayer).getValue()
                        ),
                        ComponentTranslate.trans("gca.player.ender_chest", fakePlayer.getDisplayName())
                );
            } else {
                // 打开额外功能菜单
                provider = new SimpleMenuProvider(
                        (i, inventory, p) -> ChestMenu.threeRows(
                                i, inventory,
                                GcaExtension.fakePlayerInventoryContainerMap.get(fakePlayer).getValue()
                        ),
                        ComponentTranslate.trans("gca.player.other_controller", fakePlayer.getDisplayName())
                );
            }
        } else if (GcaSetting.openFakePlayerInventory) {
            // 打开物品栏
            provider = new SimpleMenuProvider(
                    (i, inventory, p) -> new FakePlayerInventoryMenu(
                            i, inventory,
                            GcaExtension.fakePlayerInventoryContainerMap.get(fakePlayer).getKey()
                    ),
                    ComponentTranslate.trans("gca.player.inventory", fakePlayer.getDisplayName())
            );
        } else {
            return InteractionResult.PASS;
        }
        player.openMenu(provider);
        return InteractionResult.CONSUME;
    }
}
