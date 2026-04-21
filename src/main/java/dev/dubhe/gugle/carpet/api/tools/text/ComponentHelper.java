package dev.dubhe.gugle.carpet.api.tools.text;

import carpet.utils.Translations;
import dev.dubhe.gugle.carpet.GcaExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ComponentHelper {

    private static String lang = "";
    private static final Map<String, String> language = new HashMap<>();

    public static Component tr(String key, Object... args) {
        return tr(key, null, args);
    }

    public static Component tr(String key, @Nullable TextColor color, Object... args) {
        return tr(key, color, Style.EMPTY, args);
    }

    public static Component tr(String key, @Nullable TextColor color, Style style, Object... args) {
        if (color != null) style = style.withColor(color);
        // 不太懂这里为什么要try catch
        try {
            return Component.translatableWithFallback(key, language.get(key), args).setStyle(style);
        } catch (ClassCastException | NullPointerException e) {
            GcaExtension.LOGGER.error(e.getMessage(), e);
            return Component.translatable(key, args);
        }
    }

    @SuppressWarnings("NoTranslation")
    public static MutableComponent fmt(String text, Object... args) {
        return Component.translatableWithFallback("gca.format.empty", text, args);
    }

    public static Component highlight(String name) {
        return Component.literal(name).withStyle(ChatFormatting.AQUA);
    }

    public static MutableComponent fmtHlt(String text, Object... args) {
        Object[] highlights = Arrays.stream(args)
            .map(it -> {
                if (it instanceof String str) return highlight(str);
                if (it instanceof Component component) return component;
                return highlight(it.toString());
            })
            .toArray();
        return fmt(text, highlights);
    }

    public static void updateLanguage(String lang) {
        ComponentHelper.lang = lang;
        String path = String.format("assets/%s/lang/%s.json", GcaExtension.MOD_ID, lang);
        Map<String, String> translations = Translations.getTranslationFromResourcePath(path);
        language.clear();
        language.putAll(translations);
    }

    public static Map<String, String> fetchLanguage(String lang) {
        if (!ComponentHelper.lang.equals(lang)) updateLanguage(lang);
        return language;
    }
}
