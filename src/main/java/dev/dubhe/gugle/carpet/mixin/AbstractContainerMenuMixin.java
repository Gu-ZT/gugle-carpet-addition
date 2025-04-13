package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.api.menu.control.Button;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=12100
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//#else
//$$ import net.minecraft.nbt.Tag;
//#endif

@Mixin(AbstractContainerMenu.class)
abstract class AbstractContainerMenuMixin {
    @Unique
    private final AbstractContainerMenu gca$self = (AbstractContainerMenu) (Object) this;

    //#if MC < 12100
    //#elseif MC < 12105
    @Unique
    @SuppressWarnings("SameParameterValue")
    private boolean gca$getBoolean(@NotNull CustomData data, String string) {
        return data.copyTag().getBoolean(string);
    }
    //#else
    //$$ @Unique
    //$$ @SuppressWarnings("SameParameterValue")
    //$$ private boolean gca$getBoolean(@NotNull CustomData data, String string) {
    //$$     return data.copyTag().getBoolean(string).orElse(false);
    //$$ }
    //#endif

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void doClick(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (slotIndex < 0) return;
        Slot slot = gca$self.getSlot(slotIndex);
        ItemStack itemStack = slot.getItem();
        //#if MC>=12100
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.copyTag().get(Button.GCA_CLEAR) == null) {
            return;
        }
        if (this.gca$getBoolean(customData, Button.GCA_CLEAR)) {
            itemStack.setCount(0);
            ci.cancel();
        }
        //#else
        //$$ if (itemStack.getTag() == null) {
        //$$     return;
        //$$ }
        //$$ Tag tag = itemStack.getTag().get(Button.GCA_CLEAR);
        //$$ if (tag == null || !itemStack.getTag().getBoolean(Button.GCA_CLEAR)) {
        //$$     return;
        //$$ }
        //$$ itemStack.setCount(0);
        //$$ ci.cancel();
        //#endif
    }
}
