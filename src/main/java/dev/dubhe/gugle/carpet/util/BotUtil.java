package dev.dubhe.gugle.carpet.util;

import carpet.CarpetSettings;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import dev.dubhe.gugle.carpet.tools.GameProfileHelper;
import dev.dubhe.gugle.carpet.tools.player.FakePlayerSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;
//#if MC>=12102
//$$ import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
//$$ import java.util.Set;
//#else
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
//#endif

public class BotUtil {

    public static boolean spawnBot(CommandSourceStack source, @Nullable BotInfo bot) {
        if (bot == null) {
            source.sendFailure(Component.literal("%s is not exist."));
            return false;
        }
        if (source.getServer().getPlayerList().getPlayerByName(bot.name()) != null) {
            source.sendFailure(Component.literal("player %s is already exist.".formatted(bot.name())));
            return false;
        }
        source.getServer().getLevel(bot.dimType());
        try {
            ServerLevel worldIn = source.getServer().getLevel(bot.dimType());
            GameProfileCache.setUsesAuthentication(false);
            try {
                GameProfile gameprofile = getGameProfile(source.getServer(), bot.name());
                if (gameprofile == null) return false;
                SkullBlockEntity.fetchGameProfile(gameprofile.getName())
                    .thenAcceptAsync(
                        (p) -> {
                            GameProfile current = p.orElse(gameprofile);
                            if (worldIn == null) return;
                            EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(
                                source.getServer(),
                                worldIn,
                                current,
                                ClientInformation.createDefault()
                            );
                            instance.fixStartingPosition = () -> instance.moveTo(
                                bot.pos().x,
                                bot.pos().y,
                                bot.pos().z,
                                bot.facing().y,
                                bot.facing().x
                            );
                            source.getServer().getPlayerList()
                                .placeNewPlayer(
                                    new FakeClientConnection(PacketFlow.SERVERBOUND),
                                    instance,
                                    new CommonListenerCookie(current, 0, instance.clientInformation(), false)
                                );
                            //#if MC>=12102
                            //$$ instance.teleportTo(worldIn, bot.pos().x, bot.pos().y, bot.pos().z, Set.of(), bot.facing().y, bot.facing().x, true);
                            //#else
                            instance.teleportTo(worldIn, bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
                            //#endif
                            instance.setHealth(20.0F);
                            ((EntityInvoker) instance).invokeUnsetRemoved();
                            AttributeInstance attribute = instance.getAttribute(Attributes.STEP_HEIGHT);
                            if (attribute != null) attribute.setBaseValue(0.6000000238418579);
                            instance.gameMode.changeGameModeForPlayer(bot.mode());
                            source.getServer().getPlayerList()
                                .broadcastAll(
                                    new ClientboundRotateHeadPacket(
                                        instance,
                                        (byte) ((int) (instance.yHeadRot * 256.0F / 360.0F))
                                    ), bot.dimType()
                                );
                            //#if MC>=12102
                            //$$ source.getServer().getPlayerList().broadcastAll(ClientboundEntityPositionSyncPacket.of(instance), bot.dimType());
                            //#else
                            source.getServer().getPlayerList().broadcastAll(new ClientboundTeleportEntityPacket(instance), bot.dimType());
                            //#endif
                            instance.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 127);
                            instance.getAbilities().flying = bot.flying();
                            FakePlayerSerializer.applyActionPackFromJson(bot.actions(), instance);
                        }, source.getServer()
                    );
            } finally {
                GameProfileCache.setUsesAuthentication(source.getServer().isDedicatedServer() && source.getServer().usesAuthentication());
            }
            return true;
        } catch (Exception e) {
            GcaExtension.LOGGER.error("Failed to load bot: {}", bot.name(), e);
            source.sendFailure(Component.literal("%s is not loaded.".formatted(bot.name())));
            return false;
        }
    }

    @Nullable
    private static GameProfile getGameProfile(MinecraftServer server, String name) {
        GameProfileCache cache = GameProfileHelper.getProfileCache(server);
        GameProfile gameprofile = cache == null ? null : cache.get(name).orElse(null);
        if (gameprofile == null && CarpetSettings.allowSpawningOfflinePlayers) {
            gameprofile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
        }
        return gameprofile;
    }
}
