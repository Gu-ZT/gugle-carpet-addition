package dev.dubhe.gugle.carpet.tools;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.mixin.APAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FakePlayerResident {

    public static @NotNull JsonObject save(@NotNull Player player) {
        double pos_x = player.getX();
        double pos_y = player.getY();
        double pos_z = player.getZ();
        double yaw = player.getYRot();
        double pitch = player.getXRot();
        String dimension = player.level().dimension().location().getPath();
        String gamemode = ((ServerPlayer) player).gameMode.getGameModeForPlayer().getName();
        boolean flying = player.getAbilities().flying;
        JsonObject fakePlayer = new JsonObject();
        fakePlayer.addProperty("pos_x", pos_x);
        fakePlayer.addProperty("pos_y", pos_y);
        fakePlayer.addProperty("pos_z", pos_z);
        fakePlayer.addProperty("yaw", yaw);
        fakePlayer.addProperty("pitch", pitch);
        fakePlayer.addProperty("dimension", dimension);
        fakePlayer.addProperty("gamemode", gamemode);
        fakePlayer.addProperty("flying", flying);
        if (GcaSetting.fakePlayerReloadAction) {
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            fakePlayer.add("actions", apToJson(ap));
        }
        return fakePlayer;
    }

    public static void load(Map.@NotNull Entry<String, JsonElement> entry, MinecraftServer server) {
        String username = entry.getKey();
        JsonObject fakePlayer = entry.getValue().getAsJsonObject();
        double pos_x = fakePlayer.get("pos_x").getAsDouble();
        double pos_y = fakePlayer.get("pos_y").getAsDouble();
        double pos_z = fakePlayer.get("pos_z").getAsDouble();
        double yaw = fakePlayer.get("yaw").getAsDouble();
        double pitch = fakePlayer.get("pitch").getAsDouble();
        String dimension = fakePlayer.get("dimension").getAsString();
        String gamemode = fakePlayer.get("gamemode").getAsString();
        boolean flying = fakePlayer.get("flying").getAsBoolean();
        EntityPlayerMPFake playerMPFake = EntityPlayerMPFake.createFake(username, server, new Vec3(pos_x, pos_y, pos_z), yaw, pitch,
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimension)),
            GameType.byName(gamemode), flying);
        if (GcaSetting.fakePlayerReloadAction && fakePlayer.has("actions")) {
            JsonObject actions = fakePlayer.get("actions").getAsJsonObject();
            apFromJson(actions, playerMPFake);
        }
    }

    static @NotNull JsonObject apToJson(EntityPlayerActionPack ap) {
        JsonObject object = new JsonObject();
        EntityPlayerActionPack.Action attack = ((APAccessor) ap).getActions().get(EntityPlayerActionPack.ActionType.ATTACK);
        EntityPlayerActionPack.Action use = ((APAccessor) ap).getActions().get(EntityPlayerActionPack.ActionType.USE);
        EntityPlayerActionPack.Action jump = ((APAccessor) ap).getActions().get(EntityPlayerActionPack.ActionType.JUMP);
        if (attack != null && !attack.done) object.addProperty("attack", attack.interval);
        if (use != null && !use.done) object.addProperty("use", use.interval);
        if (jump != null && !jump.done) object.addProperty("jump", jump.interval);
        object.addProperty("sneaking", ((APAccessor) ap).getSneaking());
        object.addProperty("sprinting", ((APAccessor) ap).getSprinting());
        object.addProperty("forward", ((APAccessor) ap).getForward());
        object.addProperty("strafing", ((APAccessor) ap).getStrafing());
        return object;
    }

    static void apFromJson(@NotNull JsonObject actions, ServerPlayer player) {
        EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
        if (actions.has("sneaking")) ap.setSneaking(actions.get("sneaking").getAsBoolean());
        if (actions.has("sprinting")) ap.setSprinting(actions.get("sprinting").getAsBoolean());
        if (actions.has("forward")) ap.setForward(actions.get("forward").getAsFloat());
        if (actions.has("strafing")) ap.setStrafing(actions.get("strafing").getAsFloat());
        if (actions.has("attack"))
            ap.start(EntityPlayerActionPack.ActionType.ATTACK, EntityPlayerActionPack.Action.interval(actions.get("attack").getAsInt()));
        if (actions.has("use"))
            ap.start(EntityPlayerActionPack.ActionType.USE, EntityPlayerActionPack.Action.interval(actions.get("use").getAsInt()));
        if (actions.has("jump"))
            ap.start(EntityPlayerActionPack.ActionType.JUMP, EntityPlayerActionPack.Action.interval(actions.get("jump").getAsInt()));

    }
}
