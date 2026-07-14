package dev.dubhe.gugle.carpet.util;

import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.entry.BotExecutorInfo;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.CommonListenerCookie;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//#if MC < 12102
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
//#else
//$$ import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
//#endif
//#if MC >= 12110
//$$ import dev.dubhe.gugle.carpet.mixin.EntityPlayerMPFakeInvoker;
//#endif
//#if MC >= 12005
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
//#endif

public class BotUtil {
    private static final Set<String> SpawningBots = new HashSet<>();

    //#if MC >= 12104
    //$$ /**
    //$$  * Use {@link EntityPlayerMPFake#isSpawningPlayer(String)} instead in 1.21.4+.
    //$$  */
    //#endif
    public static boolean isGcaSpawningBot(String name) {
        return SpawningBots.contains(name);
    }

    public static boolean spawnBot(MinecraftServer server, BotInfo bot) {
        return spawnBot(server, bot, true);
    }

    public static boolean spawnBot(MinecraftServer server, BotInfo bot, boolean applyAction) {
        ServerLevel level = server.getLevel(bot.dimension());
        GameProfile gameProfile = GameProfileUtil.getGameProfile(server, bot.name());
        if (gameProfile == null || level == null) return false;
        String name = gameProfile.getName();

        SpawningBots.add(name);
        GameProfileUtil.fetchGameProfile(server, gameProfile).whenCompleteAsync((p, t) -> {
            SpawningBots.remove(name);
            if (t != null) return;
            GameProfile profile =
                //#if MC < 12109
                p.orElse(gameProfile);
            //#else
            //$$ p.name().isEmpty() ? gameProfile : p;
            //#endif
            spawnBot(server, level, bot, profile, applyAction, null);
        }, server);
        return true;
    }

    public static boolean spawnBot(MinecraftServer server, @Nullable ServerLevel level, BotInfo preBot, GameProfile profile, boolean applyAction, @Nullable EntityPlayerActionPack actionPack) {
        if (level == null) {
            level = server.getLevel(preBot.dimension());
            if (level == null) return false;
        }

        EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, level, profile, ClientInformation.createDefault());
        BotInfo bot = preBot.pos() == null ? preBot.withPos(instance.position()) : preBot;
        instance.fixStartingPosition = () -> instance.moveTo(bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
        server.getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), instance, new CommonListenerCookie(profile, 0, instance.clientInformation()
            //#if MC >= 12005
            , false
            //#endif
        ));
        //#if MC >= 12110
        //$$ EntityPlayerMPFakeInvoker.invokeLoadPlayerData(instance);
        //#endif
        instance.teleportTo(
            level,
            bot.pos().x,
            bot.pos().y,
            bot.pos().z,
            //#if MC >= 12102
            //$$ Set.of(),
            //#endif
            bot.facing().y,
            bot.facing().x
            //#if MC >= 12102
            //$$ ,true
            //#endif
        );
        instance.setHealth(20.0F);
        ((EntityInvoker) instance).invokeUnsetRemoved();
        //#if MC >= 12005
        AttributeInstance attribute = instance.getAttribute(Attributes.STEP_HEIGHT);
        if (attribute != null) attribute.setBaseValue(0.6F);
        //#else
        //$$ instance.setMaxUpStep(0.6F);
        //#endif
        instance.gameMode.changeGameModeForPlayer(bot.mode());
        server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(instance, (byte) (instance.yHeadRot * 256 / 360)), bot.dimension());
        server.getPlayerList().broadcastAll(
            //#if MC < 12102
            new ClientboundTeleportEntityPacket(instance),
            //#else
            //$$ ClientboundEntityPositionSyncPacket.of(instance),
            //#endif
            bot.dimension());
        instance.getEntityData().set(PlayerAccessor.getCustomisationData(), (byte) 0x7f); // show all model layers (incl. capes)
        instance.getAbilities().flying = bot.flying();
        if (applyAction) applyAction(server, instance, bot, actionPack);
        return true;
    }

    private static void applyAction(MinecraftServer server, EntityPlayerMPFake instance, BotInfo bot, @Nullable EntityPlayerActionPack actionPack) {
        List<BotExecutorInfo> startups = bot.getStartups();
        if (startups.isEmpty()) {
            bot.actions().applyAction(instance, actionPack);
            return;
        }

        for (BotExecutorInfo startup : startups) {
            try {
                server.getCommands().getDispatcher().execute(
                    startup.command(bot.name()).substring(1),
                    instance
                        // 离谱
                        //#if MC < 12102
                        .createCommandSourceStack()
                        //#else
                        //$$ .createCommandSourceStack()
                        //#endif
                );
            } catch (CommandSyntaxException e) {
                GcaExtension.LOGGER.warn("Failed to execute startup action {} for bot {}: {}", startup.desc(), bot.name(), e.getMessage());
            }
        }
    }
}
