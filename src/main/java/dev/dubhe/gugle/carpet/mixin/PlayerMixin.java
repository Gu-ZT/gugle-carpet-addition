package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
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
        // 此处不能直接使用entity instanceof EntityPlayerMPFake
        // 第一次entity instanceof Player是为了排除掉非玩家实体，避免影响非玩家实体的交互逻辑，第二次instanceof在interact方法中，用来判断交互玩家是否为假玩家
        // 在服务端，entity instanceof Player可能是多余的
        // 但是在客户端中，如果直接entity instanceof EntityPlayerMPFake，那么在右键假玩家时，客户端并不知道当前交互的玩家是不是假玩家
        // 因此右键交互时可能会应用玩家手中的物品功能，例如使用熔岩桶右键玩家时可能在假玩家位置放置岩浆，使用风弹右键玩家时可能发射风弹
        // 所以客户端在交互前要先判断一下当前交互的实体是不是玩家，这用来防止意外的使用物品功能
        // 尽管这带来了一些新的问题，例如玩家飞行时不能对着玩家使用烟花，不能对着玩家吃食物，但是这相比意外使用物品是小问题
        if (entity instanceof Player interactPlayer && (GcaSetting.openFakePlayerInventory || GcaSetting.openFakePlayerEnderChest)) {
            return interact(interactPlayer, player, hand, original);
        }
        return original.call(entity, player, hand);
    }

    @Unique
    private InteractionResult interact(Player entity, Player player, InteractionHand hand, Operation<InteractionResult> original) {
        InteractionResult result;
        if (entity instanceof EntityPlayerMPFake fakePlayer) {
            // 打开物品栏
            result = this.openInventory(player, fakePlayer);
        } else {
            // 怎么判断一个客户端玩家是不是假玩家？
            return InteractionResult.SUCCESS;
        }
        return result == InteractionResult.PASS ? original.call(entity, player, hand) : result;
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
