package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.tools.player.IClientMenuTick;
import dev.dubhe.gugle.carpet.tools.player.PlayerInventoryMenu;
import dev.dubhe.gugle.carpet.tools.player.ISlotIcon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC>=12100
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//#else
//$$ import net.minecraft.nbt.Tag;
//#endif

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ChestMenu.class)
public abstract class ChestMenuMixin implements IClientMenuTick {
    @Unique
    private final ChestMenu thisMenu = (ChestMenu) (Object) this;

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void quickMove(Player player, int i, CallbackInfoReturnable<ItemStack> cir) {
        if (this.isFakePlayerMenu()) {
            cir.setReturnValue(PlayerInventoryMenu.quickMove(thisMenu, i));
        }
    }

    @Override
    public void tick() {
        if (this.isFakePlayerMenu()) {
            ((ISlotIcon) thisMenu.getSlot(1)).setIcon(InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
            ((ISlotIcon) thisMenu.getSlot(2)).setIcon(InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
            ((ISlotIcon) thisMenu.getSlot(3)).setIcon(InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
            ((ISlotIcon) thisMenu.getSlot(4)).setIcon(InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
            ((ISlotIcon) thisMenu.getSlot(7)).setIcon(InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
        }
    }

    @Unique
    private boolean isFakePlayerMenu() {
        ItemStack itemStack = thisMenu.getSlot(0).getItem();
        //#if MC>=12100
        if (itemStack.is(Items.STRUCTURE_VOID)) {
            CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
            return customData != null && customData.copyTag().get(Button.GCA_CLEAR) != null;
        }
        //#else
        //$$ if (itemStack.is(Items.STRUCTURE_VOID) && itemStack.getTag() != null) {
        //$$     Tag tag = itemStack.getTag().get(Button.GCA_CLEAR);
        //$$     return tag != null && itemStack.getTag().getBoolean(Button.GCA_CLEAR);
        //$$ }
        //#endif
        return false;
    }
}
