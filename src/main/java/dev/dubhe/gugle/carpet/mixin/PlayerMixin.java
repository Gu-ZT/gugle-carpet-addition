package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.tools.*;
import dev.dubhe.gugle.carpet.tools.player.IClientMenuTick;
import dev.dubhe.gugle.carpet.tools.player.IGcaPlayer;
import dev.dubhe.gugle.carpet.tools.player.PlayerInventoryMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//#if MC>=12104
//$$ import net.minecraft.server.level.ServerLevel;
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
        if (this.gca$self.level().isClientSide && this.gca$self.containerMenu instanceof IClientMenuTick tick) {
            tick.tick();
        }
    }

    @WrapOperation(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult interactOn(Entity entity, @NotNull Player player, InteractionHand hand, Operation<InteractionResult> original) {
        if (player.level().isClientSide()) {
            // 客户端在交互前要先判断一下当前交互的实体是不是玩家，这用来防止意外的使用物品功能
            if (entity instanceof Player otherPlayer && ClientUtils.isFakePlayer(otherPlayer)) {
                return InteractionResult.CONSUME;
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            if ((GcaSetting.openFakePlayerInventory || SettingUtils.openFakePlayerEnderChest(player)) && entity instanceof ServerPlayer otherPlayer) {
                // 打开物品栏
                InteractionResult result = this.openInventory(serverPlayer, otherPlayer);
                if (result != InteractionResult.PASS) {
                    player.stopUsingItem();
                    return result;
                }
            }
        }
        return original.call(entity, player, hand);
    }

    @Unique
    private InteractionResult openInventory(@NotNull ServerPlayer player, @NotNull ServerPlayer otherPlayer) {
        SimpleMenuProvider provider;
        if (!(otherPlayer instanceof IGcaPlayer gcaPlayer)) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && gca$hasPremission(player, otherPlayer)) {
            // 打开末影箱
            if (SettingUtils.openFakePlayerEnderChest(player)) {
                provider = new SimpleMenuProvider(
                    (i, inventory, p) -> ChestMenu.sixRows(
                        i, inventory,
                        gcaPlayer.getEnderChestContainer()
                    ),
                    ComponentTranslate.trans("gca.player.ender_chest", otherPlayer.getDisplayName())
                );
            } else {
                // 打开额外功能菜单
                provider = new SimpleMenuProvider(
                    (i, inventory, p) -> ChestMenu.threeRows(
                        i, inventory,
                        gcaPlayer.getEnderChestContainer()
                    ),
                    ComponentTranslate.trans("gca.player.other_controller", otherPlayer.getDisplayName())
                );
            }
        } else if (GcaSetting.openFakePlayerInventory && gca$hasPremission(player, otherPlayer)) {
            // 打开物品栏
            provider = new SimpleMenuProvider(
                (i, inventory, p) -> new PlayerInventoryMenu(
                    i, inventory,
                    gcaPlayer.getInventoryContainer()
                ),
                ComponentTranslate.trans("gca.player.inventory", otherPlayer.getDisplayName())
            );
        } else {
            return InteractionResult.PASS;
        }
        player.openMenu(provider);
        return InteractionResult.CONSUME;
    }

    @Unique
    private static boolean gca$hasPremission(@NotNull ServerPlayer player, @NotNull ServerPlayer otherPlayer) {
        //#if MC>=12104
        //$$ if(!(player.level() instanceof ServerLevel serverLevel)) return otherPlayer instanceof EntityPlayerMPFake;
        //$$ CommandSourceStack stack = player.createCommandSourceStackForNameResolution(serverLevel);
        //#else
        CommandSourceStack stack = player.createCommandSourceStack();
        //#endif
        return otherPlayer instanceof EntityPlayerMPFake || CommandHelper.canUseCommand(stack, GcaSetting.openRealPlayerInventory);
    }
}
