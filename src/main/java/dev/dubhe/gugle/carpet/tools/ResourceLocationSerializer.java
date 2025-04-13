package dev.dubhe.gugle.carpet.tools;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class ResourceLocationSerializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
    public ResourceLocationSerializer() {
    }

    public ResourceLocation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        //#if MC < 12100
        //$$ return new ResourceLocation(GsonHelper.convertToString(jsonElement, "location"));
        //#else
        return ResourceLocation.parse(GsonHelper.convertToString(jsonElement, "location"));
        //#endif
    }

    public JsonElement serialize(@NotNull ResourceLocation resourceLocation, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(resourceLocation.toString());
    }
}
