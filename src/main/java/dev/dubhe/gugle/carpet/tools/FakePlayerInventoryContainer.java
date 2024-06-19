package dev.dubhe.gugle.carpet.tools;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.EntityPlayerActionPack.Action;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import com.google.common.collect.ImmutableList;
import dev.dubhe.gugle.carpet.api.menu.control.AutoResetButton;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.api.menu.control.RadioList;
import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FakePlayerInventoryContainer extends FakePlayerContainer {
    public final NonNullList<ItemStack> items;
    public final NonNullList<ItemStack> armor;
    public final NonNullList<ItemStack> offhand;
    private final NonNullList<ItemStack> buttons = NonNullList.withSize(13, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments;
    private final EntityPlayerActionPack ap;

    public FakePlayerInventoryContainer(Player player) {
        super(player);
        this.items = this.player.getInventory().items;
        this.armor = this.player.getInventory().armor;
        this.offhand = this.player.getInventory().offhand;
        this.ap = ((ServerPlayerInterface) this.player).getActionPack();
        this.compartments = ImmutableList.of(this.items, this.armor, this.offhand, this.buttons);
        this.createButton();
        this.ap.setSlot(1);
    }

    @Override
    public int getContainerSize() {
        return this.items.size() + this.armor.size() + this.offhand.size() + this.buttons.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        for (ItemStack itemStack : this.armor) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        for (ItemStack itemStack : this.offhand) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        return true;
    }

    public Map.Entry<NonNullList<ItemStack>, Integer> getItemSlot(int slot) {
        return switch (slot) {
            case 0 -> Map.entry(buttons, 0);
            case 1, 2, 3, 4 -> Map.entry(armor, 4 - slot);
            case 5, 6 -> Map.entry(buttons, slot - 4);
            case 7 -> Map.entry(offhand, 0);
            case 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 -> Map.entry(buttons, slot - 5);
            case 18, 19, 20, 21, 22, 23, 24, 25, 26,
                 27, 28, 29, 30, 31, 32, 33, 34, 35,
                 36, 37, 38, 39, 40, 41, 42, 43, 44 -> Map.entry(items, slot - 9);
            case 45, 46, 47, 48, 49, 50, 51, 52, 53 -> Map.entry(items, slot - 45);
            default -> null;
        };
    }

    @Override
    public void clearContent() {
        for (List<ItemStack> list : this.compartments) {
            list.clear();
        }
    }

    private void createButton() {
        List<Button> hotBarList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            Component hotBarComponent = ComponentTranslate.trans(
                    "gca.hotbar",
                    Color.WHITE,
                    Style.EMPTY.withBold(true).withItalic(false),
                    i + 1
            );
            boolean defaultState = i == 0;
            Button button = new Button(defaultState, i + 1,
                    hotBarComponent,
                    hotBarComponent
            );
            int finalI = i + 1;
            button.addTurnOnFunction(() -> ap.setSlot(finalI));
            this.addButton(i + 9, button);
            hotBarList.add(button);
        }
        this.addButtonList(new RadioList(hotBarList, true));

        Button stopAll = new AutoResetButton("gca.action.stop_all");
        Button attackInterval14 = new Button(false, "gca.action.attack.interval.12");
        Button attackContinuous = new Button(false, "gca.action.attack.continuous");
        Button useContinuous = new Button(false, "gca.action.use.continuous");

        stopAll.addTurnOnFunction(() -> {
            attackInterval14.turnOffWithoutFunction();
            attackContinuous.turnOffWithoutFunction();
            useContinuous.turnOffWithoutFunction();
            ap.stopAll();
        });

        attackInterval14.addTurnOnFunction(() -> {
            ap.start(ActionType.ATTACK, Action.interval(12));
            attackContinuous.turnOffWithoutFunction();
        });
        attackInterval14.addTurnOffFunction(() -> ap.start(ActionType.ATTACK, Action.once()));

        attackContinuous.addTurnOnFunction(() -> {
            ap.start(ActionType.ATTACK, Action.continuous());
            attackInterval14.turnOffWithoutFunction();
        });
        attackContinuous.addTurnOffFunction(() -> ap.start(ActionType.ATTACK, Action.once()));

        useContinuous.addTurnOnFunction(() -> ap.start(ActionType.USE, Action.continuous()));
        useContinuous.addTurnOffFunction(() -> ap.start(ActionType.USE, Action.once()));

        this.addButton(0, stopAll);
        this.addButton(5, attackInterval14);
        this.addButton(6, attackContinuous);
        this.addButton(8, useContinuous);
    }
}
