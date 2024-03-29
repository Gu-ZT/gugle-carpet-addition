package dev.dubhe.gugle.carpet.tools;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import carpet.helpers.EntityPlayerActionPack.Action;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.gugle.carpet.GcaExtension;
import net.minecraft.world.entity.player.Player;

public class FakePlayerAutoFish {

    public static void autoFish(Player player) {
        EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
        long l = player.level().getGameTime();
        GcaExtension.planFunction.add(new Pair<>(l + 5, () -> ap.start(ActionType.USE, Action.once())));
        GcaExtension.planFunction.add(new Pair<>(l + 15, () -> ap.start(ActionType.USE, Action.once())));
    }
}
