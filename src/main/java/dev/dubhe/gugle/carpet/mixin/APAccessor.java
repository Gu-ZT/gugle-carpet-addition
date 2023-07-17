package dev.dubhe.gugle.carpet.mixin;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityPlayerActionPack.class)
public interface APAccessor {
    @Accessor
    Map<EntityPlayerActionPack.ActionType, EntityPlayerActionPack.Action> getActions();

    @Accessor
    boolean getSneaking();

    @Accessor
    boolean getSprinting();

    @Accessor
    float getForward();

    @Accessor
    float getStrafing();
}
