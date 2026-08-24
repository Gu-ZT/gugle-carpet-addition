package dev.dubhe.gugle.carpet.tools.player;

import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.utils.Messenger;
import com.google.common.collect.ImmutableList;
import dev.dubhe.gugle.carpet.api.menu.control.ButtonBuilder;
import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.commands.BotCommand;
import dev.dubhe.gugle.carpet.entry.BotControllerInfo;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

//#if MC < 12003
//$$ import dev.dubhe.gugle.carpet.mixin.SimpleContainerAccessor;
//#endif

public class PlayerEnderChestContainer extends PlayerContainer {
    private static final Style TOOLTIP_STYLE = Style.EMPTY.withBold(false).withItalic(false).withColor(Color.GARY);
    public final NonNullList<ItemStack> items;
    private final NonNullList<ItemStack> buttons = NonNullList.withSize(27, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments;

    public PlayerEnderChestContainer(ServerPlayer player) {
        super(player);
        var inv = this.player.getEnderChestInventory();
        this.items =
            //#if MC >= 12003
            inv
                //#else
                //$$ ((SimpleContainerAccessor) inv)
                //#endif
                .getItems();
        this.compartments = ImmutableList.of(this.items, this.buttons);
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

    @Override
    public PlayerEnderChestContainer selfUpdate() {
        super.buttons.clear();
        this.refreshButtons();
        return this;
    }

    private void refreshButtons() {
        Map<Integer, BotControllerInfo.ControllerNode> controllers = BotCommand.controllers(this.player);

        createReplaceableButton(controllers, 0, () -> ButtonBuilder.ofKey("gca.action.sneak")
            .addTurnOnCallback(it -> this.ap.setSneaking(true))
            .addTurnOffCallback(it -> this.ap.setSneaking(false))
        );
        createReplaceableButton(controllers, 1, () -> ButtonBuilder.ofKey("gca.action.jump_continuous")
            .addTurnOnCallback(it -> this.ap.start(EntityPlayerActionPack.ActionType.JUMP, EntityPlayerActionPack.Action.continuous()))
            .addTurnOffCallback(it -> this.ap.start(EntityPlayerActionPack.ActionType.JUMP, EntityPlayerActionPack.Action.once()))
        );
        createReplaceableButton(controllers, 26, () -> ButtonBuilder.ofKey("gca.action.quit")
            .resetButton()
            .addTurnOnCallback(it -> {
                if (this.player instanceof EntityPlayerMPFake fake) fake.kill(Messenger.s("Killed"));
            })
        );

        for (int i = 2; i < 26; i++) {
            createReplaceableButton(controllers, i, () -> ButtonBuilder.ofKey("gca.button.none").noneButton());
        }
    }

    private void createReplaceableButton(
        Map<Integer, BotControllerInfo.ControllerNode> controllers,
        int slot,
        Supplier<ButtonBuilder> defaultButton
    ) {
        BotControllerInfo.ControllerNode node = controllers.get(slot + 1);
        if (node == null) {
            this.addButton(slot, defaultButton.get());
            return;
        }

        ButtonBuilder builder = ButtonBuilder.ofName(node.desc())
            .resetButton()
            .appendTooltip(Component.literal(node.command()).withStyle(TOOLTIP_STYLE))
            .addTurnOnCallback(it -> node.execute(this.player));
        this.addButton(slot, builder);
    }
}
