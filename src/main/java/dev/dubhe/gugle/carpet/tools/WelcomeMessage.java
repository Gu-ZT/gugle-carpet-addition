package dev.dubhe.gugle.carpet.tools;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import dev.dubhe.gugle.carpet.GcaExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WelcomeMessage {
    public static final String ARGS_REGEX = "\\{%\\w+%}";
    public static final FilesUtil.ObjFile<MessageConfig> WELCOME_MESSAGE = new FilesUtil.ObjFile<>("welcome", new MessageConfig());

    public static void onPlayerLoggedIn(@NotNull ServerPlayer player) {
        MessageConfig config = WELCOME_MESSAGE.obj;
        MinecraftServer server = player.getServer();
        for (String msg : config.message) {
            List<String> argKeys = new ArrayList<>();
            Matcher matcher = Pattern.compile(ARGS_REGEX).matcher(msg);
            while (matcher.find()) argKeys.add(msg.substring(matcher.start() + 2, matcher.end() - 2));
            List<String> split = new ArrayList<>(List.of(msg.split(ARGS_REGEX)));
            List<MessageData> args = argKeys.stream().map(config::getArg).toList();
            for (int i = split.size(); i < args.size() + 1; i++) split.add("");
            MutableComponent component = Component.literal("").withStyle(ChatFormatting.WHITE);
            for (int i = 0; i < split.size(); i++) {
                component.append(split.get(i));
                if (i >= args.size()) continue;
                MessageData messageData = args.get(i);
                component.append(messageData.getMsg(server, player).withStyle(messageData.color));
            }
            player.sendSystemMessage(component);
        }
    }

    public static class MessageConfig {
        public List<String> message = new ArrayList<>();
        public Map<String, MessageData> args = new HashMap<>();

        public MessageConfig() {
            message.add("{%player%}, welcome!");
            args.put("player", new MessageData());
        }

        public MessageData getArg(String key) {
            return args.getOrDefault(key, new MessageData());
        }
    }

    public static class MessageData {
        public ResourceLocation type = MessageDataType.PLAYER.location;
        public JsonElement data = null;
        public ChatFormatting color = ChatFormatting.GOLD;

        public MessageDataType getType() {
            return MessageDataType.get(type);
        }

        public MutableComponent getMsg(MinecraftServer server, ServerPlayer player) {
            return getType().getMsg(server, player, data);
        }

        public static class Serializer implements JsonSerializer<MessageData>, JsonDeserializer<MessageData> {
            @Override
            public MessageData deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                MessageData data = new MessageData();
                if (json.isJsonPrimitive()) {
                    data.type = MessageDataType.get(GcaExtension.parseLocation(json.getAsString())).location;
                    return data;
                }
                JsonObject object = json.getAsJsonObject();
                if (object.has("type")) {
                    data.type = MessageDataType.get(GcaExtension.parseLocation(object.get("type").getAsString())).location;
                }
                if (object.has("data")) {
                    data.data = object.get("data");
                }
                if (object.has("color")) {
                    data.color = ChatFormatting.getByName(object.get("color").getAsString());
                }
                return data;
            }

            @Override
            public JsonElement serialize(@NotNull MessageData src, Type typeOfSrc, JsonSerializationContext context) {
                if (src.color == ChatFormatting.GOLD && src.data == null) {
                    return new JsonPrimitive(src.type.toString());
                }
                JsonObject object = new JsonObject();
                object.addProperty("type", src.type.toString());
                if (src.data != null) object.add("data", src.data);
                if (src.color != ChatFormatting.GOLD) object.addProperty("color", src.color.toString());
                return object;
            }
        }
    }

    @FunctionalInterface
    public interface WelcomeMessageFunction {
        MutableComponent getMsg(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @Nullable JsonElement data) throws Exception;
    }

    public enum MessageDataType implements WelcomeMessageFunction {
        NONE(GcaExtension.id("none"), (s, p, d) -> Component.literal("")),
        PLAYER(GcaExtension.id("player"), (s, p, d) -> Component.literal(p.getGameProfile().getName())),
        DAYCOUNT(GcaExtension.id("day_count"), (s, p, d) -> {
            MutableComponent component = Component.literal(String.valueOf((s.overworld().getDayTime() / 1728000)));
            if (d == null || d.isJsonNull() || (!d.isJsonPrimitive() && !d.isJsonObject()) || (d.isJsonObject() && d.getAsJsonObject().asMap().isEmpty())) {
                return component;
            }
            String fromDay = "";
            if (d.isJsonPrimitive()) {
                // 2024-10-06
                fromDay = d.getAsString();
            } else if (d.isJsonObject()) {
                fromDay = d.getAsJsonObject().getAsJsonPrimitive("from").getAsString();
            }
            if (fromDay.isEmpty()) return component;
            Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC+8"));
            date.setTime(DateUtils.parseDate(fromDay, Locale.CHINA, "yyyy-MM-dd"));
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC+8"));
            if (now.getTimeInMillis() - date.getTimeInMillis() >= 0) {
                return Component.literal(String.valueOf((now.getTimeInMillis() - date.getTimeInMillis()) / 86400000 + 1));
            } else return component;
        }),
        RANDOM(GcaExtension.id("random"), (s, p, d) -> {
            List<String> args = new ArrayList<>();
            if (d != null && d.isJsonArray()) {
                for (JsonElement element : d.getAsJsonArray()) {
                    if (!element.isJsonPrimitive()) continue;
                    args.add(element.getAsString());
                }
            }
            return Component.literal(args.get(new Random().nextInt(args.size())));
        }),
        SERVER(GcaExtension.id("server"), (s, p, d) -> {
            MutableComponent component = Component.literal("").withStyle(ChatFormatting.WHITE);
            if (d == null || !d.isJsonArray()) return component;
            int i = 0;
            for (JsonElement element : d.getAsJsonArray()) {
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
                        ComponentUtils.createHoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(host))
                    );
                //#if MC>=12100
                style = style.withClickEvent(
                    host.contains(":") ?
                        ComponentUtils.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/transfer %s %s".formatted(host.split(":")[0], host.split(":")[1])) :
                        ComponentUtils.createClickEvent(ClickEvent.Action.RUN_COMMAND, "/transfer %s".formatted(host))
                );
                //#else
                //#endif
                component1.setStyle(style);
                component.append(component1);
                if (i != d.getAsJsonArray().size() - 1) component.append(Component.literal(" "));
                i++;
            }
            return Component.literal("").append(component);
        });

        public final ResourceLocation location;
        private final WelcomeMessageFunction function;

        MessageDataType(ResourceLocation location, WelcomeMessageFunction function) {
            this.location = location;
            this.function = function;
        }

        @Override
        public @NotNull String toString() {
            return this.location.toString();
        }

        public static MessageDataType get(ResourceLocation location) {
            for (MessageDataType value : values()) {
                if (value.location.equals(location)) return value;
            }
            return NONE;
        }

        @Override
        public MutableComponent getMsg(@NotNull MinecraftServer server, @NotNull ServerPlayer player, @Nullable JsonElement data) {
            try {
                return this.function.getMsg(server, player, data);
            } catch (Exception e) {
                GcaExtension.LOGGER.error(e.getMessage(), e);
            }
            return Component.literal("");
        }
    }
}
