package dev.dubhe.gugle.carpet.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.entry.WelcomeInfo;
import dev.dubhe.gugle.carpet.util.ComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//#if MC>=12100
import net.minecraft.network.chat.ClickEvent;
//#endif
//#if MC >= 260000
//$$ import net.minecraft.core.Holder;
//$$ import net.minecraft.world.clock.WorldClock;
//$$ import net.minecraft.world.level.dimension.DimensionType;
//#endif

public class WelcomeMessage {
    private static final Pattern ARGS_PATTERN = Pattern.compile("\\{%\\w+%}");
    private static final GcaConfig<WelcomeInfo> WELCOME_CONFIG = GcaConfig.create("welcome", WelcomeInfo.CODEC);
    private static final Map<ResourceLocation, WelcomeInfo.IMessageReplacer> REPLACERS = new HashMap<>();

    public static void onPlayerLoggedIn(ServerPlayer player) {
        WelcomeInfo info = getOrCreateWelcomeInfo();
        MinecraftServer server = player.level().getServer();
        if (server == null) return;
        for (String message : info.messages()) {
            MutableComponent component = Component.literal("").withStyle(ChatFormatting.WHITE);
            for (MessageSegment segment : splitSegments(message)) {
                if (!segment.arg()) {
                    component.append(segment.text());
                    continue;
                }
                String key = segment.text();
                WelcomeInfo.MessageArg arg = info.args().get(key);
                if (arg == null) {
                    GcaExtension.LOGGER.warn("No arg found for key '{}'", key);
                    component.append("{%" + key + "%}");
                    continue;
                }
                WelcomeInfo.IMessageReplacer replacer = REPLACERS.get(arg.type());
                if (replacer == null) {
                    GcaExtension.LOGGER.warn("No replacer found for welcome arg '{}'", arg.type());
                    component.append("{%" + key + "%}");
                    continue;
                }
                try {
                    component.append(replacer.getMessage(server, player, arg.data().orElse(null)).withStyle(arg.style()));
                } catch (Exception e) {
                    GcaExtension.LOGGER.error("Failed to replace welcome arg {}", key, e);
                    component.append("{%" + key + "%}");
                }
            }
            player.sendSystemMessage(component);
        }
    }

    private static WelcomeInfo getOrCreateWelcomeInfo() {
        WelcomeInfo info = WELCOME_CONFIG.get(WelcomeInfo.KEY);
        if (info == null) {
            info = WelcomeInfo.defaultInfo();
            WELCOME_CONFIG.update(info);
        }
        return info;
    }

    public static void registerReplacer(ResourceLocation location, WelcomeInfo.IMessageReplacer replacer) {
        REPLACERS.put(location, replacer);
    }

    private static List<MessageSegment> splitSegments(String msg) {
        List<MessageSegment> segments = new ArrayList<>();
        Matcher matcher = ARGS_PATTERN.matcher(msg);
        int cursor = 0;
        while (matcher.find()) {
            if (cursor < matcher.start()) {
                segments.add(new MessageSegment(msg.substring(cursor, matcher.start()), false));
            }
            String key = msg.substring(matcher.start() + 2, matcher.end() - 2);
            segments.add(new MessageSegment(key, true));
            cursor = matcher.end();
        }
        if (cursor < msg.length()) {
            segments.add(new MessageSegment(msg.substring(cursor), false));
        }
        return segments;
    }

    private record MessageSegment(String text, boolean arg) {
    }

    public static void registerDefaultReplacer() {
        registerReplacer(GcaExtension.id("player"), (server, player, args) ->
            player.getDisplayName().copy()
        );
        registerReplacer(GcaExtension.id("day_count"), (server, player, args) -> {
            //#if MC < 260000
            long ticks = server.overworld().getDayTime();
            //#else
            //$$ Holder<DimensionType> dimension = server.overworld().dimensionTypeRegistration();
            //$$ Holder<WorldClock> clock = dimension.value().defaultClock().orElse(null);
            //$$ if (clock == null) {
            //$$     GcaExtension.LOGGER.warn("No clock found for dimension '{}'", dimension.getRegisteredName());
            //$$     return Component.literal("No clock found for dimension '%s'".formatted(dimension.getRegisteredName())).withStyle(ChatFormatting.RED);
            //$$ }
            //$$ long ticks = server.clockManager()
                     //#if MC < 260300
                     //$$ .getTotalTicks(clock)
                     //#else
                     //$$ .getInstance(clock).totalTicks()
                     //#endif
            //$$     ;
            //#endif
            MutableComponent component = Component.literal(String.valueOf(ticks / 1728000));
            if (args == null || args.isJsonNull() || (!args.isJsonPrimitive() && !args.isJsonObject()) || (
                args.isJsonObject() && args.getAsJsonObject().asMap().isEmpty()
            )) {
                return component;
            }
            String fromDay = "";
            if (args.isJsonPrimitive()) {
                // 2024-10-06
                fromDay = args.getAsString();
            } else if (args.isJsonObject()) {
                fromDay = args.getAsJsonObject().getAsJsonPrimitive("from").getAsString();
            }
            if (fromDay.isEmpty()) return component;
            Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC+8"));
            date.setTime(DateUtils.parseDate(fromDay, Locale.CHINA, "yyyy-MM-dd"));
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC+8"));
            if (now.getTimeInMillis() - date.getTimeInMillis() >= 0) {
                return Component.literal(String.valueOf((now.getTimeInMillis() - date.getTimeInMillis()) / 86400000 + 1));
            } else {
                return component;
            }
        });
        registerReplacer(GcaExtension.id("random"), (server, player, args) -> {
            if (args != null && args.isJsonArray()) {
                List<String> list = args.getAsJsonArray()
                    .asList()
                    .stream()
                    .filter(JsonElement::isJsonPrimitive)
                    .map(JsonElement::getAsString)
                    .toList();
                if (!list.isEmpty()) {
                    RandomSource random = RandomSource.create();
                    return Component.literal(list.get(random.nextInt(list.size())));
                }
            }
            return Component.literal("[EMPTY]");
        });
        registerReplacer(GcaExtension.id("server"), (server, player, args) -> {
            MutableComponent component = Component.literal("");
            if (args == null || !args.isJsonArray()) return component;
            int i = 0;
            for (JsonElement element : args.getAsJsonArray()) {
                if (!element.isJsonPrimitive() && !element.isJsonObject()) continue;
                String name;
                String host;
                if (element.isJsonPrimitive()) {
                    name = "[Server]";
                    host = element.getAsString();
                } else {
                    JsonObject object = element.getAsJsonObject();
                    name = object.has("name") ? "[%s]".formatted(object.get("name").getAsString()) : "[Server]";
                    host = object.has("host") ? object.get("host").getAsString() : "";
                }
                MutableComponent component1 = Component.literal(name);
                Style style = Style.EMPTY.applyFormat(ChatFormatting.GREEN)
                    .withHoverEvent(
                        ComponentUtil.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(host))
                    );
                //#if MC>=12100
                style = style.withClickEvent(
                    host.contains(":") ?
                        ComponentUtil.createClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/transfer %s %s".formatted(host.split(":")[0], host.split(":")[1])
                        ) :
                        ComponentUtil.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/transfer %s".formatted(host))
                );
                //#else
                //#endif
                component1.setStyle(style);
                component.append(component1);
                if (i != args.getAsJsonArray().size() - 1) component.append(Component.literal(" "));
                i++;
            }
            return Component.literal("").append(component);
        });

    }
}
