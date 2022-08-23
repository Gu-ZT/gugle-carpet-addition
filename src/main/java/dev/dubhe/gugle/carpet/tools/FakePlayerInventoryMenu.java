package dev.dubhe.gugle.carpet.tools;

import carpet.fakes.ServerPlayerEntityInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.EntityPlayerActionPack.Action;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.gugle.carpet.api.menu.CustomMenu;
import dev.dubhe.gugle.carpet.api.menu.CustomMenuType.Type;
import dev.dubhe.gugle.carpet.api.menu.control.AutoResetButton;
import dev.dubhe.gugle.carpet.api.menu.control.Button;
import dev.dubhe.gugle.carpet.api.menu.control.RadioList;
import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FakePlayerInventoryMenu extends CustomMenu {

    private final EntityPlayerActionPack ap;
    private final List<Pair<Integer, Pair<NonNullList<ItemStack>, Integer>>> slotMap;

    public FakePlayerInventoryMenu(Player player) {
        super(Type.GENERIC_9x6);
        this.slotMap = this.mapping(player);
        this.ap = ((ServerPlayerEntityInterface) player).getActionPack();
        this.createButton();
    }

    @Override
    public void tick() {
        this.syncItem();
        super.tick();
    }

    public void syncItem() {
        for (Pair<Integer, Pair<NonNullList<ItemStack>, Integer>> slot : slotMap) {
            int index = slot.getFirst();
            NonNullList<ItemStack> inv = slot.getSecond().getFirst();
            int index2 = slot.getSecond().getSecond();
            if (this.getItem(index) == inv.get(index2) || this.getItem(index).isEmpty()) {
                this.setItem(index, inv.get(index2));
            } else {
                inv.set(index2, this.getItem(index));
            }
        }
    }

    private List<Pair<Integer, Pair<NonNullList<ItemStack>, Integer>>> mapping(Player player) {
        List<Pair<Integer, Pair<NonNullList<ItemStack>, Integer>>> slotMap = new ArrayList<>();
        Inventory inv = player.getInventory();

        for (int i = 0; i <= 3; i++) {
            slotMap.add(new Pair<>(4 - i, new Pair<>(inv.armor, i)));
        }
        for (int i = 0; i <= 8; i++) {
            slotMap.add(new Pair<>(45 + i, new Pair<>(inv.items, i)));
        }
        for (int i = 9; i <= 35; i++) {
            slotMap.add(new Pair<>(9 + i, new Pair<>(inv.items, i)));
        }
        slotMap.add(new Pair<>(7, new Pair<>(inv.offhand, 0)));

        return slotMap;
    }

    private void createButton() {
        List<Button> hotBarList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            Component hotBarComponent = ComponentTranslate.trans(
                    "gac.hotbar",
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

        Button stopAll = new AutoResetButton("action.stop_all");
        Button attackInterval14 = new Button(false, "action.attack.interval.14");
        Button attackContinuous = new Button(false, "action.attack.continuous");
        Button useContinuous = new Button(false, "action.use.continuous");

        stopAll.addTurnOnFunction(() -> {
            attackInterval14.turnOffWithoutFunction();
            attackContinuous.turnOffWithoutFunction();
            useContinuous.turnOffWithoutFunction();
            ap.stopAll();
        });

        attackInterval14.addTurnOnFunction(() -> {
            ap.start(ActionType.ATTACK, Action.interval(14));
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
