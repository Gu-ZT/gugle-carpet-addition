package dev.dubhe.gugle.carpet.config.updater.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.lang.reflect.Type;

public class DimTypeSerializer implements JsonSerializer<ResourceKey<Level>>, JsonDeserializer<ResourceKey<Level>> {
    @Override
    public ResourceKey<Level> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return ResourceKey.create(
            Registries.DIMENSION,
            //#if MC>=12100
            ResourceLocation.parse(json.getAsString())
            //#else
            //$$ new ResourceLocation(json.getAsString())
            //#endif
        );
    }

    @Override
    public JsonElement serialize(ResourceKey<Level> src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.location().toString());
    }
}
