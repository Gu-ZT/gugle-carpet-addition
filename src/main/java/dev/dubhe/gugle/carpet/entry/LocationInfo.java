package dev.dubhe.gugle.carpet.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record LocationInfo(
    long id,
    String desc,
    Vec3 pos,
    ResourceKey<Level> dimType
) implements IWithName {
    public static final Codec<LocationInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("id").forGetter(LocationInfo::id),
        Codec.STRING.fieldOf("desc").forGetter(LocationInfo::desc),
        Vec3.CODEC.fieldOf("pos").forGetter(LocationInfo::pos),
        ResourceKey.codec(Registries.DIMENSION).fieldOf("dim_type").forGetter(LocationInfo::dimType)
    ).apply(instance, LocationInfo::new));

    @Override
    public String name() {
        return String.valueOf(this.id);
    }
}
