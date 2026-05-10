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
    private static final Map<String, String> en_us = new HashMap<>();

    public static MutableComponent tr(String key, Object... args) {
        return tr(key, null, Style.EMPTY, args);
    }

    public static MutableComponent tr(String key, @Nullable TextColor color, Object... args) {
        return tr(key, color, Style.EMPTY, args);
    }

    public static MutableComponent tr(String key, @Nullable TextColor color, Style style, Object... args) {
        if (color != null) style = style.withColor(color);
        String text = language.get(key);
        return Component.translatableWithFallback(key, text, args).setStyle(style);
    }

    @SuppressWarnings("NoTranslation")
    public static MutableComponent fmt(String text, Object... args) {
        return Component.translatableWithFallback("gca.format.empty", text, args);
    }

    public static Component highlight(Object value) {
        return Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GOLD);
    }

    public static Component fmtTr(String key, Object... args) {
        Object[] highlights = Arrays.stream(args)
            .map(it -> {
                if (it instanceof Component component) return component;
                if (it instanceof String str) return highlight(str);
                return highlight(it.toString());
            })
            .toArray();
        return tr(key, highlights);
    }

    public static Component prefix(Component content) {
        return Component.literal("")
            .append(Component.literal("[GCA]").withStyle(ChatFormatting.DARK_AQUA))
            .append(" ")
            .append(content);
    }

    public static Component intro(Component content) {
        return fmt("======== %s ========", content).withStyle(ChatFormatting.GRAY);
    }

    public static void updateLanguage(String lang) {
        if (en_us.isEmpty()) {
            String path = String.format("assets/%s/lang/%s.json", GcaExtension.MOD_ID, "en_us");
            Map<String, String> translations = Translations.getTranslationFromResourcePath(path);
            en_us.putAll(translations);
        }
        language.clear();
        language.putAll(en_us);
        ComponentHelper.lang = lang;
        if (!"en_us".equals(lang)) {
            String path = String.format("assets/%s/lang/%s.json", GcaExtension.MOD_ID, lang);
            language.putAll(Translations.getTranslationFromResourcePath(path));
        }
    }

    public static Map<String, String> fetchLanguage(String lang) {
        if (!ComponentHelper.lang.equals(lang)) updateLanguage(lang);
        return language;
    }
}
