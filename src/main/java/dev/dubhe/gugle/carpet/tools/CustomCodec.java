//#if MC < 12105
package dev.dubhe.gugle.carpet.tools;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class CustomCodec {
    public static final Codec<Vec2> VEC2_CODEC = Codec.FLOAT
        .listOf()
        .comapFlatMap(
            list -> Util.fixedSize(list, 2).map(it -> new Vec2(it.get(0), it.get(1))),
            vec2 -> List.of(vec2.x, vec2.y)
        );
}
//#endif
