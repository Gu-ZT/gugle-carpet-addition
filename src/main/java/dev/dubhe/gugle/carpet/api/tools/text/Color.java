package dev.dubhe.gugle.carpet.api.tools.text;

import net.minecraft.network.chat.TextColor;

@SuppressWarnings("unused")
public class Color {

    public static final TextColor BLACK = TextColor.parseColor("#000000").getOrThrow();

    public static final TextColor DARK_BLUE = TextColor.parseColor("#0000AA").getOrThrow();

    public static final TextColor DARK_GREEN = TextColor.parseColor("#00AA00").getOrThrow();

    public static final TextColor DARK_AQUA = TextColor.parseColor("#00AAAA").getOrThrow();

    public static final TextColor DARK_RED = TextColor.parseColor("#AA0000").getOrThrow();

    public static final TextColor DARK_PURPLE = TextColor.parseColor("#AA00AA").getOrThrow();

    public static final TextColor GOLD = TextColor.parseColor("#FFAA00").getOrThrow();

    public static final TextColor GARY = TextColor.parseColor("#AAAAAA").getOrThrow();

    public static final TextColor DARK_GARY = TextColor.parseColor("#555555").getOrThrow();

    public static final TextColor BLUE = TextColor.parseColor("#5555FF").getOrThrow();

    public static final TextColor GREEN = TextColor.parseColor("#55FF55").getOrThrow();

    public static final TextColor AQUA = TextColor.parseColor("#55FFFF").getOrThrow();

    public static final TextColor RED = TextColor.parseColor("#FF5555").getOrThrow();

    public static final TextColor LIGHT_PURPLE = TextColor.parseColor("#FF55FF").getOrThrow();

    public static final TextColor YELLOW = TextColor.parseColor("#FFFF55").getOrThrow();

    public static final TextColor WHITE = TextColor.parseColor("#FFFFFF").getOrThrow();

    private Color() {
    }
}
