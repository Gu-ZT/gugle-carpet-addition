package dev.dubhe.gugle.carpet.api.tools.text;

import carpet.CarpetSettings;
import carpet.utils.Translations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.dubhe.gugle.carpet.GcaExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class ComponentTranslate {

    private final Map<String, String> lang = getTranslations(CarpetSettings.language);

    public static Component trans(String key, Object... args) {
        return trans(key, null, args);
    }

    public static Component trans(String key, TextColor color, Object... args) {
        return trans(key, color, Style.EMPTY, args);
    }

    public static Component trans(String key, TextColor color, Style style, Object... args) {
        ComponentTranslate componentTranslate = new ComponentTranslate();
        if (color != null) style = style.withColor(color);
        if (componentTranslate.lang != null) {
            Object[] callbackArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object object = args[i];
                if (object instanceof Component component) {
                    object = component.getString();
                }
                callbackArgs[i] = object;
            }
            String callback = componentTranslate.lang.get(key).formatted(callbackArgs);
            try {
                return Component
                    .translatableWithFallback(key, callback, args)
                    .setStyle(style);
            } catch (ClassCastException | NullPointerException e) {
                GcaExtension.LOGGER.error(e.getMessage(), e);
            }
        }
        return Component.translatable(key, args);
    }

    public static @Nullable Map<String, String> getTranslations(String lang) {
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
