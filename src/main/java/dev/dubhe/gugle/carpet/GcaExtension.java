package dev.dubhe.gugle.carpet;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.utils.Translations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class GcaExtension implements CarpetExtension, ModInitializer {

    public static String MOD_ID = "gca";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    static
    {
        CarpetServer.manageExtension(new GcaExtension());
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(GcaSetting.class);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        String dataJSON;
        try {
            dataJSON = IOUtils.toString(
                    Objects.requireNonNull(Translations.class.getClassLoader().getResourceAsStream(
                            String.format("assets/gca/lang/%s.json", lang))),
                    StandardCharsets.UTF_8);
        } catch (NullPointerException | IOException e) {
            return null;
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.fromJson(dataJSON, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    @Override
    public void onInitialize() {

    }
}
