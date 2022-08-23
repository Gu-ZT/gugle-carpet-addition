package dev.dubhe.gugle.carpet;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.tools.FakePlayerInventoryMenu;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GcaExtension implements CarpetExtension, ModInitializer {

    public static String MOD_ID = "gca";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final HashMap<Player, FakePlayerInventoryMenu> fakePlayerInventoryMenuHashMap = new HashMap<>();

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    static {
        CarpetServer.manageExtension(new GcaExtension());
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(GcaSetting.class);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return ComponentTranslate.getTranslations(lang);
    }

    @Override
    public void onPlayerLoggedIn(ServerPlayer player) {
        if (player instanceof EntityPlayerMPFake) {
            GcaExtension.fakePlayerInventoryMenuHashMap.put(player, new FakePlayerInventoryMenu(player));
        }
    }

    @Override
    public void onPlayerLoggedOut(ServerPlayer player) {
        if (player instanceof EntityPlayerMPFake) {
            GcaExtension.fakePlayerInventoryMenuHashMap.get(player).clearAllItem();
            GcaExtension.fakePlayerInventoryMenuHashMap.remove(player);
        }
    }

    @Override
    public void onInitialize() {

    }
}
