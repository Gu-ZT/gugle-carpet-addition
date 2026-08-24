package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import dev.dubhe.gugle.carpet.tools.player.IClientMenuTick;
import dev.dubhe.gugle.carpet.tools.player.IGcaPlayer;
import dev.dubhe.gugle.carpet.tools.player.PlayerInventoryMenu;
import dev.dubhe.gugle.carpet.util.ClientUtil;
import dev.dubhe.gugle.carpet.util.SettingUtil;
import net.minecraft.commands.CommandSourceStack;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//#if MC>=12102 && MC <= 12105
//$$ import net.minecraft.server.level.ServerLevel;
//#endif
//#if MC >= 260000
//$$ import net.minecraft.world.phys.Vec3;
//#endif

@Mixin(Player.class)
abstract class PlayerMixin {
    @Unique
    private final Player gca$self = (Player) (Object) this;

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        if (this.gca$self.isAlive() && this.gca$self instanceof IGcaPlayer gcaPlayer) {
            gcaPlayer.getEnderChestContainer().tick();
            gcaPlayer.getInventoryContainer().tick();
        }
        if (this.gca$self.level().isClientSide() && this.gca$self.containerMenu instanceof IClientMenuTick tick) {
            tick.tick();
        }
    }

    @WrapOperation(
        method = "interactOn",
        at = @At(
            value = "INVOKE",
            target =
                //#if MC < 260000
                "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
                //#else
                //$$ "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;"
                //#endif
        )
    )
    private InteractionResult interactOn(Entity entity, Player player, InteractionHand hand,
                                         //#if MC >= 260000
                                         //$$ Vec3 pos,
                                         //#endif
                                         Operation<InteractionResult> original) {
        if (player.level().isClientSide()) {
            // 客户端在交互前要先判断一下当前交互的实体是不是玩家，这用来防止意外的使用物品功能
            if (entity instanceof Player otherPlayer && ClientUtil.isFakePlayer(otherPlayer)) {
                return InteractionResult.CONSUME;
            }
        } else if (player instanceof ServerPlayer serverPlayer && entity instanceof ServerPlayer otherPlayer) {
            InteractionResult result = this.openInventory(serverPlayer, otherPlayer);
            if (result != InteractionResult.PASS) {
                player.stopUsingItem();
                return result;
            }
        }
        return original.call(entity, player, hand
            //#if MC >= 260000
            //$$ , pos
            //#endif
        );
    }

    @Unique
    private InteractionResult openInventory(ServerPlayer player, ServerPlayer otherPlayer) {
        if (!(otherPlayer instanceof IGcaPlayer gcaPlayer)) return InteractionResult.PASS;

        SimpleMenuProvider provider = null;
        boolean isFakePlayer = otherPlayer instanceof EntityPlayerMPFake;
        boolean canOperateRealPlayer = !isFakePlayer && gca$canOperateRealPlayer(player);
        boolean canOperate = isFakePlayer || canOperateRealPlayer;
        boolean canOpenInventory = canOperateRealPlayer || (isFakePlayer && GcaSetting.openFakePlayerInventory);

        if (canOperate && player.isShiftKeyDown()) {
            // 打开末影箱
            if (SettingUtil.openFakePlayerEnderChest(player)) {
                provider = new SimpleMenuProvider(
                    (i, inventory, p) -> ChestMenu.sixRows(
                        i, inventory,
                        gcaPlayer.getEnderChestContainer().selfUpdate()
                    ),
                    ComponentHelper.tr("gca.player.ender_chest", otherPlayer.getDisplayName())
                );
            } else if (canOpenInventory) {
                // 打开额外功能菜单
                provider = new SimpleMenuProvider(
                    (i, inventory, p) -> ChestMenu.threeRows(
                        i, inventory,
                        gcaPlayer.getEnderChestContainer().selfUpdate()
                    ),
                    ComponentHelper.tr("gca.player.other_controller", otherPlayer.getDisplayName())
                );
            }
        } else if (canOpenInventory) {
            // 打开物品栏
            provider = new SimpleMenuProvider(
                (i, inventory, p) -> new PlayerInventoryMenu(
                    i, inventory,
                    gcaPlayer.getInventoryContainer().selfUpdate()
                ),
                ComponentHelper.tr("gca.player.inventory", otherPlayer.getDisplayName())
            );
        }

        if (provider == null) return InteractionResult.PASS;

        player.openMenu(provider);
        return InteractionResult.CONSUME;
    }

    @Unique
    private static boolean gca$canOperateRealPlayer(ServerPlayer player) {
        CommandSourceStack stack = player.createCommandSourceStack(
            //#if MC>=12102
            //#if MC<=12105
            //$$ (ServerLevel)
            //#endif
            //$$ player.level()
            //#endif
        );
        return CommandHelper.canUseCommand(stack, GcaSetting.openRealPlayerInventory);
    }
}
