package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.tools.player.ISlotIcon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#if MC>=12104
//#else
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.InventoryMenu;
//#endif

@Mixin(Slot.class)
public class SlotMixin implements ISlotIcon {
    @Unique
    //#if MC>=12104
    //$$ private ResourceLocation location;
    //#else
    private Pair<ResourceLocation, ResourceLocation> pair;
    //#endif

    @Inject(method = "getNoItemIcon", at = @At("HEAD"), cancellable = true)
    private void getNoItemIcon(
        //#if MC>=12104
        //$$ CallbackInfoReturnable<ResourceLocation> cir
        //#else
        CallbackInfoReturnable<Pair<ResourceLocation, ResourceLocation>> cir
        //#endif
    ) {
        //#if MC>=12104
        //$$ cir.setReturnValue(this.location);
        //#else
        if (this.pair != null) cir.setReturnValue(this.pair);
        //#endif
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public void setIcon(ResourceLocation resource) {
        if (resource != null) {
            //#if MC>=12104
            //$$ this.location = resource;
            //#else
            this.pair = Pair.of(InventoryMenu.BLOCK_ATLAS, resource);
            //#endif
        }
    }
}
