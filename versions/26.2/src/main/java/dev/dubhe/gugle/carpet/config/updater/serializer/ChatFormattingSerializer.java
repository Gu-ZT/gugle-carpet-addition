package dev.dubhe.gugle.carpet.config.updater.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;

public class ChatFormattingSerializer implements JsonSerializer<ChatFormatting>, JsonDeserializer<ChatFormatting> {
    public static final Codec<ChatFormatting> CODEC = Codec.STRING.comapFlatMap(
        name -> DataResult.success(ChatFormatting.valueOf(name.toUpperCase())),
        chatFormatting -> chatFormatting.name().toLowerCase()
    );

    @Override
    public ChatFormatting deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Optional.ofNullable(ChatFormattingSerializer.byName(json.getAsString())).orElse(ChatFormatting.WHITE);
    }

    @Override
    public JsonElement serialize(ChatFormatting src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }

    public static @Nullable ChatFormatting byName(String name) {
        try {
            return ChatFormatting.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
