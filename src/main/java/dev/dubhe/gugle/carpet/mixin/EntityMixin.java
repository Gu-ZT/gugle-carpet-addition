package dev.dubhe.gugle.carpet.mixin;

import static dev.dubhe.gugle.carpet.tools.FakePlayer.*;

import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    SimpleContainer container = new SimpleContainer(54);
    Entity self = (Entity) (Object) this;
    boolean[] flag = new boolean[14];

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (self instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof EntityPlayerMPFake fakePlayer) {
                if (!flag[13]) {
                    for (int i = 0; i < 14; i++) {
                        flag[i] = true;
                    }
                }
                EntityPlayerActionPack ap = new EntityPlayerActionPack(serverPlayer);
                syncItem(fakePlayer, container);
                CompoundTag GcaClear = new CompoundTag();
                GcaClear.putBoolean("GcaClear", true);
                ItemStack button = new ItemStack(Items.STRUCTURE_VOID, 1);
                button.setTag(GcaClear);
                flag[0] = checkButton(0, flag[0], container, 1);
                flag[1] = checkButton(5, flag[1], container, 1);
                flag[2] = checkButton(6, flag[2], container, 1);
                flag[3] = checkButton(8, flag[3], container, 1);
                for (int i = 9; i < 18; i++) {
                    boolean bl = checkButton(i, flag[i - 5], container, i - 8,
                            Component.literal("快捷栏：" + (i - 8)),
                            Component.literal("快捷栏：" + (i - 8))
                    );
                    if (bl != flag[i - 5]) {
                        for (int j = 4; j < 13; j++) {
                            flag[j] = false;
                        }
                        flag[i - 5] = bl;
                    }
                }
                for (int i = 1; i < 10; i++) {
                    if (flag[i + 3]) {
                        ap.setSlot(i);
                    }
                }
            } else {
                ItemStack carried = serverPlayer.containerMenu.getCarried();
                if (carried.getTag() != null) {
                    if (carried.getTag().get("GcaClear") != null) {
                        if (carried.getTag().getBoolean("GcaClear")) {
                            carried.setCount(0);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (GcaSetting.openFakePlayerInventory) {
            if (self.getLevel().isClientSide()) {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }
            if (self instanceof ServerPlayer selfPlayer) {
                if (selfPlayer instanceof EntityPlayerMPFake) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        SimpleMenuProvider provider = new SimpleMenuProvider(
                                (i, inventory, p) -> ChestMenu.sixRows(i, inventory, container),
                                selfPlayer.getDisplayName());
                        serverPlayer.openMenu(provider);
                        cir.setReturnValue(InteractionResult.SUCCESS);
                    }
                }
            }
        }
    }
}
