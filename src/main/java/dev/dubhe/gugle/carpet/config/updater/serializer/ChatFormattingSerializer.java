package dev.dubhe.gugle.carpet.config.updater.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;

//#if MC >= 260200
//$$ import com.mojang.serialization.DataResult;
//#endif

public class ChatFormattingSerializer implements JsonSerializer<ChatFormatting>, JsonDeserializer<ChatFormatting> {
    public static final Codec<ChatFormatting> CODEC =
        //#if MC < 260200
        ChatFormatting.CODEC
        //#else
        //$$ Codec.STRING.comapFlatMap(
        //$$     name -> DataResult.success(ChatFormatting.valueOf(name.toUpperCase())),
        //$$     chatFormatting -> chatFormatting.name().toLowerCase()
        //$$ )
        //#endif
        ;

    @Override
    public ChatFormatting deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Optional.ofNullable(byName(json.getAsString())).orElse(ChatFormatting.WHITE);
    }

    @Override
    public JsonElement serialize(ChatFormatting src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(
            //#if MC < 260200
            src.getName()
            //#else
            //$$ src.name()
            //#endif
        );
    }

    @Nullable
    public static ChatFormatting byName(String name) {
        //#if MC < 260200
        return ChatFormatting.getByName(name);
        //#else
        //$$     try {
        //$$         return ChatFormatting.valueOf(name.toUpperCase());
        //$$     } catch (IllegalArgumentException e) {
        //$$         return null;
        //$$     }
        //#endif
    }
}
