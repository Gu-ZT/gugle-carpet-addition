package dev.dubhe.gugle.carpet.util;

import carpet.patches.EntityPlayerMPFake;
import carpet.patches.FakeClientConnection;
import com.mojang.authlib.GameProfile;
import dev.dubhe.gugle.carpet.entry.BotInfo;
import dev.dubhe.gugle.carpet.mixin.EntityInvoker;
import dev.dubhe.gugle.carpet.mixin.PlayerAccessor;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.CommonListenerCookie;

import java.util.HashSet;
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
        return spawnBot(server, bot, true, false);
    }

    public static boolean spawnBot(MinecraftServer server, BotInfo bot, boolean applyAction, boolean isRespawn) {
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

            EntityPlayerMPFake instance = EntityPlayerMPFake.respawnFake(server, level, profile, ClientInformation.createDefault());
            instance.fixStartingPosition = () -> instance.moveTo(bot.pos().x, bot.pos().y, bot.pos().z, bot.facing().y, bot.facing().x);
            server.getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), instance, new CommonListenerCookie(profile, 0, instance.clientInformation()
                //#if MC >= 12005
                , false
                //#endif
            ));
            //#if MC >= 12110
            //$$ EntityPlayerMPFakeInvoker.invokeLoadPlayerData(instance);
            //#endif
            // respawn fake player keep riding
            if (!isRespawn) instance.stopRiding();
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
            instance.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6F);
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
            if (applyAction) bot.actions().applyAction(instance);
        }, server);
        return true;
    }
}
