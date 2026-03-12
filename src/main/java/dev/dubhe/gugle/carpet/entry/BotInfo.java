package dev.dubhe.gugle.carpet.entry;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.gugle.carpet.tools.CustomCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record BotInfo(
    String name,
    String desc,
    Vec3 pos,
    Vec2 facing,
    ResourceKey<Level> dimension,
    GameType mode,
    boolean flying,
    BotActionInfo actions
) implements IWithName {
    public static final Codec<BotInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(BotInfo::name),
        Codec.STRING.fieldOf("desc").forGetter(BotInfo::desc),
        Vec3.CODEC.fieldOf("pos").forGetter(BotInfo::pos),
        CustomCodec.VEC2_CODEC.fieldOf("facing").forGetter(BotInfo::facing),
        ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(BotInfo::dimension),
        GameType.CODEC.fieldOf("mode").forGetter(BotInfo::mode),
        Codec.BOOL.fieldOf("flying").forGetter(BotInfo::flying),
        BotActionInfo.CODEC.fieldOf("actions").forGetter(BotInfo::actions)
    ).apply(instance, BotInfo::new));

    public static BotInfo create(ServerPlayer player, boolean saveAction) {
        String name = player.getGameProfile()
            //#if MC < 12110
            .getName();
        //#else
        //$$ .name();
        //#endif
        EntityPlayerActionPack actionPack = saveAction ?
            ((ServerPlayerInterface) player).getActionPack() :
            new EntityPlayerActionPack(player);
        return BotInfo.create(name, name, player, actionPack);
    }

    public static BotInfo create(String name, String desc, ServerPlayer player, EntityPlayerActionPack actionPack) {
        return new BotInfo(
            name,
            desc,
            player.position(),
            player.getRotationVector(),
            player.level().dimension(),
            player.gameMode.getGameModeForPlayer(),
            player.getAbilities().flying,
            BotActionInfo.fromActionPack(actionPack)
        );
    }
}
