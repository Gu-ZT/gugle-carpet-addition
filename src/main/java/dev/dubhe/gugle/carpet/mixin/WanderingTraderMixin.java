package dev.dubhe.gugle.carpet.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dubhe.gugle.carpet.GcaSetting;
import dev.dubhe.gugle.carpet.api.tools.text.Color;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import dev.dubhe.gugle.carpet.tools.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;


@Mixin(WanderingTraderSpawner.class)
abstract class WanderingTraderMixin {
    @Unique
    private MinecraftServer gca$server = null;
    @Unique
    private ServerPlayer gca$player = null;
    @Unique
    private int gca$llama = 0;
    @Shadow
    private int spawnChance;

    @Inject(method = "tick", at = @At("HEAD"))
    //#if MC >= 12105
    //$$ private void spawn(@NotNull ServerLevel serverLevel, boolean bl, boolean bl2, CallbackInfo ci) {
    //#else
    private void spawn(@NotNull ServerLevel serverLevel, boolean bl, boolean bl2, CallbackInfoReturnable<Integer> cir) {
        //#endif
        this.gca$server = serverLevel.getServer();
        this.gca$llama = 0;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int spawn0(RandomSource instance, int i, @NotNull Operation<Integer> original) {
        int result = original.call(instance, i);
        if (result > this.spawnChance) {
            this.gca$sendMsg(
                ComponentTranslate.trans(
                    "carpet.rule.wanderingTraderSpawnFailedWarning.tip.02",
                    Color.YELLOW,
                    Style.EMPTY,
                    "i <= %s".formatted(this.spawnChance),
                    result
                )
            );
        }
        return result;
    }

    @WrapOperation(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int spawn1(RandomSource instance, int i, @NotNull Operation<Integer> original) {
        int result = original.call(instance, i);
        if (result != 0) {
            this.gca$sendMsg(
                ComponentTranslate.trans(
                    "carpet.rule.wanderingTraderSpawnFailedWarning.tip.02",
                    Color.YELLOW,
                    Style.EMPTY,
                    "i == 0",
                    result
                )
            );
        }
        return result;
    }

    @WrapOperation(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getRandomPlayer()Lnet/minecraft/server/level/ServerPlayer;"))
    private @Nullable ServerPlayer getRandomPlayer(ServerLevel instance, @NotNull Operation<ServerPlayer> original) {
        this.gca$player = original.call(instance);
        return this.gca$player;
    }

    @Inject(method = "spawn", at = @At(value = "RETURN", ordinal = 2))
    private void spawn2(ServerLevel serverLevel, CallbackInfoReturnable<Boolean> cir) {
        this.gca$sendMsg(
            ComponentTranslate.trans(
                "carpet.rule.wanderingTraderSpawnFailedWarning.tip.03",
                Color.YELLOW,
                Style.EMPTY,
                this.gca$player.getDisplayName()
            )
        );
    }

    @Inject(method = "spawn", at = @At(value = "RETURN", ordinal = 4))
    private void spawnSuccess(ServerLevel serverLevel, CallbackInfoReturnable<Boolean> cir) {
        this.gca$sendMsg(
            ComponentTranslate.trans(
                "carpet.rule.wanderingTraderSpawnFailedWarning.tip.04",
                Color.YELLOW,
                Style.EMPTY,
                this.gca$player.getDisplayName()
            )
        );
    }

    @Inject(method = "tryToSpawnLlamaFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/TraderLlama;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V"))
    private void tryToSpawnLlamaFor(ServerLevel serverLevel, WanderingTrader wanderingTrader, int i, CallbackInfo ci) {
        this.gca$llama++;
    }

    @SuppressWarnings("InjectLocalCaptureCanBeReplacedWithLocal")
    @Inject(method = "spawn", at = @At(value = "RETURN", ordinal = 3), locals = LocalCapture.CAPTURE_FAILHARD)
    private void spawn3(
        ServerLevel serverLevel,
        CallbackInfoReturnable<Boolean> cir,
        Player player,
        BlockPos blockPos,
        int i,
        PoiManager poiManager,
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<BlockPos> optional,
        BlockPos blockPos2,
        BlockPos blockPos3
    ) {
        if (!GcaSetting.wanderingTraderSpawnRemind) return;
        Vec3 center = blockPos3.getCenter();
        this.gca$server.getPlayerList().broadcastSystemMessage(
            ComponentTranslate.trans(
                "carpet.rule.wanderingTraderSpawnRemind.tip",
                Color.YELLOW,
                Style.EMPTY,
                Component.literal("[%.1f, %.1f, %.1f]".formatted(center.x, center.y, center.z))
                    .withStyle(
                        Style.EMPTY
                            .withColor(ChatFormatting.GREEN)
                            .withClickEvent(
                                ComponentUtils.createClickEvent(
                                    ClickEvent.Action.SUGGEST_COMMAND,
                                    "/tp @s %.1f %.1f %.1f".formatted(center.x, center.y, center.z)
                                )
                            )
                    ),
                Component.literal(String.valueOf(this.gca$llama))
                    .withStyle(ChatFormatting.GREEN)
            ),
            false
        );
    }

    @Unique
    private void gca$sendMsg(Component msg) {
        if (!GcaSetting.wanderingTraderSpawnFailedWarning) return;
        if (this.gca$server == null) return;
        this.gca$server.getPlayerList().broadcastSystemMessage(
            ComponentTranslate.trans(
                "carpet.rule.wanderingTraderSpawnFailedWarning.tip.01",
                Color.YELLOW,
                Style.EMPTY
            ),
            false
        );
        this.gca$server.getPlayerList().broadcastSystemMessage(msg, false);
    }
}
