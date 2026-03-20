package dev.dubhe.gugle.carpet.util;

import net.minecraft.world.item.ItemStack;
//#if MC < 12005
//$$ import net.minecraft.nbt.CompoundTag;
//$$ import net.minecraft.nbt.Tag;
//#else
import net.minecraft.core.component.DataComponents;
//#endif

public class ContainerUtil {
    public static boolean hasContainer(ItemStack stack) {
        //#if MC>=12005
        return stack.has(DataComponents.CONTAINER);
        //#else
        //$$ CompoundTag tag = stack.getTag();
        //$$ if (tag == null) return false;
        //$$ return tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("Items", Tag.TAG_LIST);
        //#endif
    }

}
