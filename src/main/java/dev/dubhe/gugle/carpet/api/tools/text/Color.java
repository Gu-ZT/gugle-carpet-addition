package dev.dubhe.gugle.carpet.api.tools.text;

import net.minecraft.network.chat.TextColor;

public class Color {
    public static final TextColor BLACK = Color.color("#000000");

    public static final TextColor DARK_BLUE = Color.color("#0000AA");

    public static final TextColor DARK_GREEN = Color.color("#00AA00");

    public static final TextColor DARK_AQUA = Color.color("#00AAAA");

    public static final TextColor DARK_RED = Color.color("#AA0000");

    public static final TextColor DARK_PURPLE = Color.color("#AA00AA");

    public static final TextColor GOLD = Color.color("#FFAA00");

    public static final TextColor GARY = Color.color("#AAAAAA");

    public static final TextColor DARK_GARY = Color.color("#555555");

    public static final TextColor BLUE = Color.color("#5555FF");

    public static final TextColor GREEN = Color.color("#55FF55");

    public static final TextColor AQUA = Color.color("#55FFFF");

    public static final TextColor RED = Color.color("#FF5555");

    public static final TextColor LIGHT_PURPLE = Color.color("#FF55FF");

    public static final TextColor YELLOW = Color.color("#FFFF55");

    public static final TextColor WHITE = Color.color("#FFFFFF");

    private Color() {
    }

    private static TextColor color(String color) {
        //#if MC>=12100
        return TextColor.parseColor(color).getOrThrow();
        //#else
        //$$ return TextColor.parseColor(color);
        //#endif
    }
}
