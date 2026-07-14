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
) implements IFakePlayerAction {
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

    public void applyAction(ServerPlayer player, @Nullable EntityPlayerActionPack actionPack) {
        EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
        IFakePlayerAction action = actionPack == null ? this : IFakePlayerAction.of(actionPack);
        ap.setSneaking(action.getSneaking());
        ap.setSprinting(action.getSprinting());
        ap.setForward(action.getForward());
        ap.setStrafing(action.getStrafing());
        action.applyAction(ap);
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

    @Override
    public boolean getSneaking() {
        return this.sneaking;
    }

    @Override
    public boolean getSprinting() {
        return this.sprinting;
    }

    @Override
    public float getForward() {
        return this.forward;
    }

    @Override
    public float getStrafing() {
        return this.strafing;
    }

    @Override
    public void applyAction(EntityPlayerActionPack actionPack) {
        setActionInterval(actionPack, EntityPlayerActionPack.ActionType.ATTACK, this.attack);
        setActionInterval(actionPack, EntityPlayerActionPack.ActionType.USE, this.use);
        setActionInterval(actionPack, EntityPlayerActionPack.ActionType.JUMP, this.jump);
    }
}
