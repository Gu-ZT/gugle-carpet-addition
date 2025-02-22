package dev.dubhe.gugle.carpet.tools.player;

import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.utils.Messenger;
import com.google.common.collect.ImmutableList;
import dev.dubhe.gugle.carpet.api.menu.control.AutoResetButton;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerEnderChestContainer extends PlayerContainer {
    public final NonNullList<ItemStack> items;
    private final NonNullList<ItemStack> buttons = NonNullList.withSize(27, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments;

    public PlayerEnderChestContainer(ServerPlayer player) {
        super(player);
        this.items = this.player.getEnderChestInventory().items;
        this.compartments = ImmutableList.of(this.items, this.buttons);
        this.createButton();
    }

    @Override
    public int getContainerSize() {
        return this.items.size() + this.buttons.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        return true;
    }

    public Map.Entry<NonNullList<ItemStack>, Integer> getItemSlot(int slot) {
        if (slot > 26) {
            return Map.entry(items, slot - 27);
        } else {
            return Map.entry(buttons, slot);
        }
    }

    @Override
    public void clearContent() {
        for (List<ItemStack> list : this.compartments) {
            list.clear();
        }
    }

    private void createButton() {
        List<Integer> slots = new ArrayList<>();
        Button sneakButton = new Button(false, "gca.action.sneak");
        sneakButton.addTurnOnFunction(() -> this.ap.setSneaking(true));
        sneakButton.addTurnOffFunction(() -> this.ap.setSneaking(false));
        this.addButton(0, sneakButton);
        Button jumpButton = new Button(false, "gca.action.jump_continuous");
        jumpButton.addTurnOnFunction(() -> this.ap.start(EntityPlayerActionPack.ActionType.JUMP, EntityPlayerActionPack.Action.continuous()));
        jumpButton.addTurnOffFunction(() -> this.ap.start(EntityPlayerActionPack.ActionType.JUMP, EntityPlayerActionPack.Action.once()));
        this.addButton(1, jumpButton);
        AutoResetButton quitButton = new AutoResetButton("gca.action.quit");
        quitButton.addTurnOnFunction(() -> {
            if (this.player instanceof EntityPlayerMPFake fake) fake.kill(Messenger.s("Killed"));
        });
        this.addButton(26, quitButton);
        for (Map.Entry<Integer, Button> button : super.buttons) {
            slots.add(button.getKey());
        }
        for (int i = 0; i < 27; i++) {
            if (slots.contains(i)) continue;
            this.addButton(i, AutoResetButton.NONE);
        }
    }
}
