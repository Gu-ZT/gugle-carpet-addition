package dev.dubhe.gugle.carpet.tools.player;

import carpet.helpers.EntityPlayerActionPack.Action;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import com.google.common.collect.ImmutableList;
import dev.dubhe.gugle.carpet.api.menu.control.ButtonBuilder;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentHelper;
import dev.dubhe.gugle.carpet.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class PlayerInventoryContainer extends PlayerContainer {
    public final NonNullList<ItemStack> items;
    public final NonNullList<ItemStack> armor;
    public final NonNullList<ItemStack> offhand;
    private final NonNullList<ItemStack> buttons = NonNullList.withSize(13, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments;
    private final List<Button> hotbar;
    private final Button attack12;
    private final Button attackContinuous;
    private final Button useContinuous;

    public PlayerInventoryContainer(ServerPlayer player) {
        super(player);
        this.items = InventoryUtil.getItems(this.player);
        this.armor = InventoryUtil.getArmor(this.player);
        this.offhand = InventoryUtil.getOffHand(this.player);
        this.compartments = ImmutableList.of(this.items, this.armor, this.offhand, this.buttons);
        this.hotbar = PlayerInventoryContainer.createHotbarButtons(this, this::addButton);

        ButtonBuilder attack12 = ButtonBuilder.ofKey("gca.action.attack.interval.12")
            .addTurnOnCallback(button -> ap.start(ActionType.ATTACK, Action.interval(12)))
            .addTurnOffCallback(button -> this.ap.start(ActionType.ATTACK, Action.once()));

        ButtonBuilder attackContinuous = ButtonBuilder.ofKey("gca.action.attack.continuous")
            .addTurnOnCallback(button -> this.ap.start(ActionType.ATTACK, Action.continuous()))
            .addTurnOffCallback(button -> this.ap.start(ActionType.ATTACK, Action.once()));

        ButtonBuilder useContinuous = ButtonBuilder.ofKey("gca.action.use.continuous")
            .addTurnOnCallback(button -> this.ap.start(ActionType.USE, Action.continuous()))
            .addTurnOffCallback(button -> this.ap.start(ActionType.USE, Action.once()));

        attack12.addTurnOnCallback(button -> attackContinuous.get().changeStatus(false, true));
        attackContinuous.addTurnOnCallback(button -> attack12.get().changeStatus(false, true));

        ButtonBuilder stopAll = ButtonBuilder.ofKey("gca.action.stop_all").resetButton()
            .addTurnOnCallback(button -> {
                attack12.get().changeStatus(false, true);
                attackContinuous.get().changeStatus(false, true);
                useContinuous.get().changeStatus(false, true);
                this.ap.stopAll();
            });

        this.addButton(0, stopAll);
        this.attack12 = Objects.requireNonNull(this.addButton(5, attack12));
        this.attackContinuous = Objects.requireNonNull(this.addButton(6, attackContinuous));
        this.useContinuous = Objects.requireNonNull(this.addButton(8, useContinuous));
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

    @Nullable
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

    private static List<Button> createHotbarButtons(PlayerInventoryContainer container, BiFunction<Integer, ButtonBuilder, Button> factory) {
        return IntStream.range(0, 9).mapToObj(slot -> {
            int num = slot + 1;
            Component component = ComponentHelper.tr(
                "gca.hotbar",
                Color.WHITE,
                Style.EMPTY.withBold(true).withItalic(false),
                num
            );
            ButtonBuilder builder = ButtonBuilder.ofComponent(component)
                .setItemCount(num)
                .addTurnOnCallback(button -> container.ap.setSlot(num));
            return factory.apply(slot + 9, builder);
        }).toList();
    }

    @Override
    public void tick() {
        super.tick();
        int selected = InventoryUtil.getSelected(this.player);
        for (int i = 0; i < this.hotbar.size(); i++) {
            Button button = this.hotbar.get(i);
            button.changeStatus(i == selected, true);
        }
    }

    public void resetAttackButton() {
        this.attack12.softChangeStatus(false);
        this.attackContinuous.softChangeStatus(false);
    }

    public void resetUseButton() {
        this.useContinuous.softChangeStatus(false);
    }
}
