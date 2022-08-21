package dev.dubhe.gugle.carpet.mixin;

import static dev.dubhe.gugle.carpet.tools.FakePlayer.*;
import static dev.dubhe.gugle.carpet.tools.FakePlayer.ComponentTrans.trans;

import carpet.fakes.ServerPlayerEntityInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.EntityPlayerActionPack.Action;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.nbt.CompoundTag;
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
                    container.setItem(0, new ItemStack(Items.BARRIER));
                    container.setItem(5, new ItemStack(Items.BARRIER));
                    container.setItem(6, new ItemStack(Items.BARRIER));
                    container.setItem(8, new ItemStack(Items.BARRIER));
                    for (int i = 0; i < 9; i++) {
                        container.setItem(i + 9, new ItemStack(Items.BARRIER));
                    }
                    for (int i = 0; i < 13; i++) {
                        flag[i] = false;
                    }
                    flag[13] = true;
                }
                syncItem(fakePlayer, container);
                CompoundTag GcaClear = new CompoundTag();
                GcaClear.putBoolean("GcaClear", true);
                EntityPlayerActionPack ap = ((ServerPlayerEntityInterface) fakePlayer).getActionPack();
                ItemStack button = new ItemStack(Items.STRUCTURE_VOID, 1);
                button.setTag(GcaClear);
                flag[0] = checkButton(0, flag[0], container, 1,
                        trans("action.stop_all"),
                        trans("action.stop_all"),
                        (container, slot) -> {
                            ap.stopAll();
                            flag[1] = false;
                            flag[2] = false;
                            flag[3] = false;
                        },
                        null,
                        serverPlayer,
                        false
                );
                flag[1] = checkButton(5, flag[1], container, 1,
                        trans("action.attack.continuous", "on"),
                        trans("action.attack.continuous", "off"),
                        (container, slot) -> {
                            ap.start(ActionType.ATTACK, Action.continuous());
                            flag[3] = false;
                        },
                        (container, slot) -> ap.start(ActionType.ATTACK, Action.once()),
                        serverPlayer
                );
                flag[2] = checkButton(6, flag[2], container, 1,
                        trans("action.use.continuous", "on"),
                        trans("action.use.continuous", "off"),
                        (container, slot) -> ap.start(ActionType.USE, Action.continuous()),
                        (container, slot) -> ap.start(ActionType.USE, Action.once()),
                        serverPlayer
                );
                flag[3] = checkButton(8, flag[3], container, 1,
                        trans("action.attack.interval.14", "on"),
                        trans("action.attack.interval.14", "off"),
                        (container, slot) -> {
                            ap.start(ActionType.ATTACK, Action.interval(14));
                            flag[1] = false;
                        },
                        (container, slot) -> ap.start(ActionType.ATTACK, Action.once()),
                        serverPlayer
                );
                for (int i = 9; i < 18; i++) {
                    int finalI = i - 8;
                    boolean bl = checkButton(i, flag[i - 5], container, i - 8,
                            trans("gac.hotbar", i - 8),
                            trans("gac.hotbar", i - 8),
                            (container, slot) -> ap.setSlot(finalI),
                            null,
                            serverPlayer
                    );
                    if (bl != flag[i - 5]) {
                        for (int j = 4; j < 13; j++) {
                            flag[j] = false;
                        }
                        flag[i - 5] = bl;
                    }
                }
                if (!(flag[4] || flag[5] || flag[6] || flag[7] || flag[8] || flag[9] || flag[10] || flag[11]
                        || flag[12])) {
                    container.removeItem(9,1);
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
                        SimpleMenuProvider provider;
                        if (!serverPlayer.isShiftKeyDown()) {
                            provider = new SimpleMenuProvider(
                                    (i, inventory, p) -> ChestMenu.sixRows(i, inventory, container),
                                    selfPlayer.getDisplayName());
                        } else {
                            provider = new SimpleMenuProvider(
                                    (i, inventory, p) -> ChestMenu.threeRows(i, inventory,
                                            selfPlayer.getEnderChestInventory()),
                                    selfPlayer.getDisplayName());
                        }
                        serverPlayer.openMenu(provider);
                        cir.setReturnValue(InteractionResult.SUCCESS);
                    }
                }
            }
        }
    }
}
