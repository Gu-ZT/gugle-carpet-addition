package dev.dubhe.gugle.carpet.tools.player;

import carpet.helpers.EntityPlayerActionPack.Action;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import com.google.common.collect.ImmutableList;
import dev.dubhe.gugle.carpet.api.menu.control.AutoResetButton;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.api.menu.control.RadioList;
import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.tools.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class PlayerInventoryContainer extends PlayerContainer {
    public final NonNullList<ItemStack> items;
    public final NonNullList<ItemStack> armor;
    public final NonNullList<ItemStack> offhand;
    private final NonNullList<ItemStack> buttons = NonNullList.withSize(13, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments;
    private final RadioList hotbar;

    public PlayerInventoryContainer(ServerPlayer player) {
        super(player);
        this.items = InventoryUtil.getItems(this.player);
        this.armor = InventoryUtil.getArmor(this.player);
        this.offhand = InventoryUtil.getOffHand(this.player);
        this.compartments = ImmutableList.of(this.items, this.armor, this.offhand, this.buttons);
        this.hotbar = PlayerInventoryContainer.createHotbarButton(this::addButton, this);
        this.createButton();
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

    private static @NotNull RadioList createHotbarButton(BiConsumer<Integer, Button> adder, PlayerInventoryContainer container) {
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
            button.addTurnOnFunction(() -> container.ap.setSlot(finalI));
            adder.accept(i + 9, button);
            hotBarList.add(button);
        }
        return new RadioList(hotBarList, true);
    }

    private void createButton() {
        this.addButtonList(this.hotbar);

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

    @Override
    public void tick() {
        super.tick();
        List<Button> buttonList = this.hotbar.getButtons();
        for (int i = 0; i < buttonList.size(); i++) {
            if (i == InventoryUtil.getSelected(this.player)) {
                buttonList.get(i).turnOnWithoutFunction();
            } else {
                buttonList.get(i).turnOffWithoutFunction();
            }
        }
    }
}
