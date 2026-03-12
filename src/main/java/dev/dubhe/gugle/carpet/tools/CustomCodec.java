package dev.dubhe.gugle.carpet.tools;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class CustomCodec {
    public static final Codec<Vec2> VEC2_CODEC = Codec.FLOAT
        .listOf()
        .comapFlatMap(
            list -> Util.fixedSize(list, 2).map(it -> new Vec2(it.get(0), it.get(1))),
            vec2 -> List.of(vec2.x, vec2.y)
        );

    public static final Codec<JsonObject> JSON_CODEC = ExtraCodecs.JSON.comapFlatMap(
        it -> {
            if (!it.isJsonObject()) return DataResult.error(() -> "Expected a JSON object");
            return DataResult.success(it.getAsJsonObject());
        },
        it -> it
    );

}
