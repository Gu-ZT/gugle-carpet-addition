package dev.dubhe.gugle.carpet.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
//#if MC<12109
import net.minecraft.world.entity.player.Player;
//#else
//$$ import net.minecraft.world.entity.Avatar;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//#if MC<12109
@Mixin(Player.class)
//#else
//$$ @Mixin(Avatar.class)
//#endif
public interface PlayerAccessor {
    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION")
    static EntityDataAccessor<Byte> getCustomisationData() {
        throw new AssertionError();
    }
}
