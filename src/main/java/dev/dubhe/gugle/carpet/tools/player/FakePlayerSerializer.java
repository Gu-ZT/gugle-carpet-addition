package dev.dubhe.gugle.carpet.tools.player;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import com.google.gson.JsonObject;
import dev.dubhe.gugle.carpet.mixin.APAccessor;
import dev.dubhe.gugle.carpet.mixin.ActionAccessor;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class FakePlayerSerializer {

    public static @NotNull JsonObject actionPackToJson(EntityPlayerActionPack actionPack) {
        JsonObject object = new JsonObject();
        EntityPlayerActionPack.Action attack = ((APAccessor) actionPack).getActions().get(EntityPlayerActionPack.ActionType.ATTACK);
        EntityPlayerActionPack.Action use = ((APAccessor) actionPack).getActions().get(EntityPlayerActionPack.ActionType.USE);
        EntityPlayerActionPack.Action jump = ((APAccessor) actionPack).getActions().get(EntityPlayerActionPack.ActionType.JUMP);

        if (attack != null && !attack.done) {
            object.addProperty("attack", attack.interval * (((ActionAccessor) attack).isContinuous() ? -1 : 1));
        }
        if (use != null && !use.done) {
            object.addProperty("use", use.interval * (((ActionAccessor) use).isContinuous() ? -1 : 1));
        }
        if (jump != null && !jump.done) {
            object.addProperty("jump", jump.interval * (((ActionAccessor) jump).isContinuous() ? -1 : 1));
        }
        object.addProperty("sneaking", ((APAccessor) actionPack).getSneaking());
        object.addProperty("sprinting", ((APAccessor) actionPack).getSprinting());
        object.addProperty("forward", ((APAccessor) actionPack).getForward());
        object.addProperty("strafing", ((APAccessor) actionPack).getStrafing());
        return object;
    }

    public static void applyActionPackFromJson(JsonObject actions, ServerPlayer player) {
        EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
        if (actions.has("sneaking")) ap.setSneaking(actions.get("sneaking").getAsBoolean());
        if (actions.has("sprinting")) ap.setSprinting(actions.get("sprinting").getAsBoolean());
        if (actions.has("forward")) ap.setForward(actions.get("forward").getAsFloat());
        if (actions.has("strafing")) ap.setStrafing(actions.get("strafing").getAsFloat());
        if (actions.has("attack")) {
            int attack = actions.get("attack").getAsInt();
            if (attack < 0) {
                ap.start(EntityPlayerActionPack.ActionType.ATTACK, EntityPlayerActionPack.Action.continuous());
            } else {
                ap.start(EntityPlayerActionPack.ActionType.ATTACK, EntityPlayerActionPack.Action.interval(attack));
            }
        }
        if (actions.has("use")) {
            int use = actions.get("use").getAsInt();
            if (use < 0) {
                ap.start(EntityPlayerActionPack.ActionType.USE, EntityPlayerActionPack.Action.continuous());
            } else {
                ap.start(EntityPlayerActionPack.ActionType.USE, EntityPlayerActionPack.Action.interval(use));
            }
        }
        if (actions.has("jump")) {
            int jump = actions.get("jump").getAsInt();
            if (jump < 0) {
                ap.start(EntityPlayerActionPack.ActionType.JUMP, EntityPlayerActionPack.Action.continuous());
            } else {
                ap.start(EntityPlayerActionPack.ActionType.JUMP, EntityPlayerActionPack.Action.interval(jump));
            }
        }
    }
}
