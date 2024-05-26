package dev.dubhe.gugle.carpet.mixin;

import carpet.helpers.EntityPlayerActionPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityPlayerActionPack.class)
public interface APAccessor {
    @Accessor(remap = false)
    Map<EntityPlayerActionPack.ActionType, EntityPlayerActionPack.Action> getActions();

    @Accessor(remap = false)
    boolean getSneaking();

    @Accessor(remap = false)
    boolean getSprinting();

    @Accessor(remap = false)
    float getForward();

    @Accessor(remap = false)
    float getStrafing();
}
