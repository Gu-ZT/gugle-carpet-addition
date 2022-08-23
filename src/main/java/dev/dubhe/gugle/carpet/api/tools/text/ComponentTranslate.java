package dev.dubhe.gugle.carpet.api.tools.text;

import carpet.CarpetSettings;
import carpet.utils.Translations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class ComponentTranslate {

    private final Map<String, String> lang = getTranslations(CarpetSettings.language);

    public static Component trans(String key, Object... args) {
        return trans(key, Color.WHITE, args);
    }

    public static Component trans(String key, TextColor color, Object... args){
        return trans(key, color, Style.EMPTY, args);
    }

    public static Component trans(String key, TextColor color, Style style, Object... args) {
        ComponentTranslate componentTranslate = new ComponentTranslate();
        if (componentTranslate.lang != null) {
            try {
                return Component.literal(
                        componentTranslate.lang
                                .get(key)
                                .formatted(args)
                ).setStyle(style.withColor(color));
            } catch (ClassCastException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return Component.literal(key.formatted(args));
    }

    public static Map<String, String> getTranslations(String lang) {
        String dataJSON;
        try {
            dataJSON = IOUtils.toString(
                    Objects.requireNonNull(
                            Translations.class
                                    .getClassLoader()
                                    .getResourceAsStream(String.format("assets/gca/lang/%s.json", lang))
                    ),
                    StandardCharsets.UTF_8
            );
        } catch (NullPointerException | IOException e) {
            try {
                dataJSON = IOUtils.toString(
                        Objects.requireNonNull(
                                Translations.class
                                        .getClassLoader()
                                        .getResourceAsStream("assets/gca/lang/en_us.json")
                        ),
                        StandardCharsets.UTF_8
                );
            } catch (NullPointerException | IOException ex) {
                return null;
            }
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.fromJson(dataJSON, new TypeToken<Map<String, String>>() {
        }.getType());
    }
}
