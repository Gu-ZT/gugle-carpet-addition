package dev.dubhe.gugle.carpet.api.tools.text;

import net.minecraft.network.chat.TextColor;

public class Color {

    public static final TextColor BLACK = TextColor.parseColor("#000000").result().orElseThrow();

    public static final TextColor DARK_BLUE = TextColor.parseColor("#0000AA").result().orElseThrow();

    public static final TextColor DARK_GREEN = TextColor.parseColor("#00AA00").result().orElseThrow();

    public static final TextColor DARK_AQUA = TextColor.parseColor("#00AAAA").result().orElseThrow();

    public static final TextColor DARK_RED = TextColor.parseColor("#AA0000").result().orElseThrow();

    public static final TextColor DARK_PURPLE = TextColor.parseColor("#AA00AA").result().orElseThrow();

    public static final TextColor GOLD = TextColor.parseColor("#FFAA00").result().orElseThrow();

    public static final TextColor GARY = TextColor.parseColor("#AAAAAA").result().orElseThrow();

    public static final TextColor DARK_GARY = TextColor.parseColor("#555555").result().orElseThrow();

    public static final TextColor BLUE = TextColor.parseColor("#5555FF").result().orElseThrow();

    public static final TextColor GREEN = TextColor.parseColor("#55FF55").result().orElseThrow();

    public static final TextColor AQUA = TextColor.parseColor("#55FFFF").result().orElseThrow();

    public static final TextColor RED = TextColor.parseColor("#FF5555").result().orElseThrow();

    public static final TextColor LIGHT_PURPLE = TextColor.parseColor("#FF55FF").result().orElseThrow();

    public static final TextColor YELLOW = TextColor.parseColor("#FFFF55").result().orElseThrow();

    public static final TextColor WHITE = TextColor.parseColor("#FFFFFF").result().orElseThrow();

    private Color() {
    }
}
