package dev.dubhe.gugle.carpet.entry;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.mixin.APAccessor;
import dev.dubhe.gugle.carpet.mixin.ActionAccessor;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public record BotActionInfo(
    boolean sneaking,
    boolean sprinting,
    float forward,
    float strafing,
    int attack,
    int use,
    int jump
) {
    public static final Codec<BotActionInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("sneaking", false).forGetter(BotActionInfo::sneaking),
        Codec.BOOL.optionalFieldOf("sprinting", false).forGetter(BotActionInfo::sprinting),
        Codec.FLOAT.optionalFieldOf("forward", 0F).forGetter(BotActionInfo::forward),
        Codec.FLOAT.optionalFieldOf("strafing", 0F).forGetter(BotActionInfo::strafing),
        Codec.INT.optionalFieldOf("attack", 0).forGetter(BotActionInfo::attack),
        Codec.INT.optionalFieldOf("use", 0).forGetter(BotActionInfo::use),
        Codec.INT.optionalFieldOf("jump", 0).forGetter(BotActionInfo::jump)
    ).apply(instance, BotActionInfo::new));

    public static final BotActionInfo EMPTY = new BotActionInfo(false, false, 0F, 0F, 0, 0, 0);

    public static BotActionInfo fromActionPack(@Nullable EntityPlayerActionPack pack) {
        if (pack == null) return EMPTY;
        APAccessor accessor = (APAccessor) pack;
        return new BotActionInfo(
            accessor.getSneaking(),
            accessor.getSprinting(),
            accessor.getForward(),
            accessor.getStrafing(),
            getActionInterval(accessor, EntityPlayerActionPack.ActionType.ATTACK),
            getActionInterval(accessor, EntityPlayerActionPack.ActionType.USE),
            getActionInterval(accessor, EntityPlayerActionPack.ActionType.JUMP)
        );
    }

    public void applyAction(ServerPlayer player) {
        EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
        ap.setSneaking(this.sneaking);
        ap.setSprinting(this.sprinting);
        ap.setForward(this.forward);
        ap.setStrafing(this.strafing);
        setActionInterval(ap, EntityPlayerActionPack.ActionType.ATTACK, this.attack);
        setActionInterval(ap, EntityPlayerActionPack.ActionType.USE, this.use);
        setActionInterval(ap, EntityPlayerActionPack.ActionType.JUMP, this.jump);
    }

    private static int getActionInterval(APAccessor accessor, EntityPlayerActionPack.ActionType type) {
        EntityPlayerActionPack.Action action = accessor.getActions().get(type);
        if (action == null || action.done) return 0;
        if (((ActionAccessor) action).isContinuous()) return -1;
        return action.interval;
    }

    public static void setActionInterval(EntityPlayerActionPack ap, EntityPlayerActionPack.ActionType type, int interval) {
        if (interval == 0) return;
        EntityPlayerActionPack.Action action = interval < 0 ?
            EntityPlayerActionPack.Action.continuous() :
            EntityPlayerActionPack.Action.interval(interval);
        ap.start(type, action);
    }

}
