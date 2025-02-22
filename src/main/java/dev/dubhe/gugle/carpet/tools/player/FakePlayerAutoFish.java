package dev.dubhe.gugle.carpet.tools.player;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import carpet.helpers.EntityPlayerActionPack.Action;
import dev.dubhe.gugle.carpet.GcaExtension;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FakePlayerAutoFish {

    public static void autoFish(@NotNull Player player) {
        EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
        long l = player.level().getGameTime();
        GcaExtension.PLAN_FUNCTION.add(Map.entry(l + 5, () -> ap.start(ActionType.USE, Action.once())));
        GcaExtension.PLAN_FUNCTION.add(Map.entry(l + 15, () -> ap.start(ActionType.USE, Action.once())));
    }
}
