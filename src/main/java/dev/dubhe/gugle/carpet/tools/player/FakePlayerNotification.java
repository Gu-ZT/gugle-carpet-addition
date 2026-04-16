package dev.dubhe.gugle.carpet.tools.player;

import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FakePlayerNotification {

    @SuppressWarnings("resource")
    private static void broadcastSystemMessage(ServerPlayer player, String key, ItemStack stack) {
        Component playerName = player.getDisplayName();
        Component itemName = stack.getDisplayName();

        Component msg = ComponentTranslate.trans(key, playerName, itemName);

        player.level().getServer().getPlayerList().broadcastSystemMessage(msg, false);
    }

    public static void sendToolDamaged(ServerPlayer player, ItemStack stack) {
        broadcastSystemMessage(player, "gca.tool.damaged", stack);
    }

    public static void sendRestockFailed(ServerPlayer player, ItemStack stack) {
        broadcastSystemMessage(player, "gca.tool.restock.failed", stack);
    }

    public static void sendRestockFailed(ServerPlayer player, Item item) {
        broadcastSystemMessage(player, "gca.tool.restock.failed", item.getDefaultInstance());
    }

}
