package dev.dubhe.gugle.carpet.entry;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.mixin.APAccessor;
import dev.dubhe.gugle.carpet.mixin.ActionAccessor;
import net.minecraft.server.level.ServerPlayer;

public record BotActionInfo(
    int attack,
    int use,
    int jump,
    boolean sneaking,
    boolean sprinting,
    float forward,
    float strafing
) {
    public static final Codec<BotActionInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("attack").forGetter(BotActionInfo::attack),
        Codec.INT.fieldOf("use").forGetter(BotActionInfo::use),
        Codec.INT.fieldOf("jump").forGetter(BotActionInfo::jump),
        Codec.BOOL.fieldOf("sneaking").forGetter(BotActionInfo::sneaking),
        Codec.BOOL.fieldOf("sprinting").forGetter(BotActionInfo::sprinting),
        Codec.FLOAT.fieldOf("forward").forGetter(BotActionInfo::forward),
        Codec.FLOAT.fieldOf("strafing").forGetter(BotActionInfo::strafing)
    ).apply(instance, BotActionInfo::new));

    public static BotActionInfo fromActionPack(EntityPlayerActionPack pack) {
        APAccessor accessor = (APAccessor) pack;
        return new BotActionInfo(
            getActionInterval(accessor, EntityPlayerActionPack.ActionType.ATTACK),
            getActionInterval(accessor, EntityPlayerActionPack.ActionType.USE),
            getActionInterval(accessor, EntityPlayerActionPack.ActionType.JUMP),
            accessor.getSneaking(),
            accessor.getSprinting(),
            accessor.getForward(),
            accessor.getStrafing()
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
